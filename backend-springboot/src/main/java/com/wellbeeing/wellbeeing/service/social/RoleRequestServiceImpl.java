package com.wellbeeing.wellbeeing.service.social;

import com.wellbeeing.wellbeeing.domain.account.*;
import com.wellbeeing.wellbeeing.domain.exception.ForbiddenException;
import com.wellbeeing.wellbeeing.domain.exception.IllegalArgumentException;
import com.wellbeeing.wellbeeing.domain.social.EStatus;
import com.wellbeeing.wellbeeing.domain.social.RoleRequest;
import com.wellbeeing.wellbeeing.repository.account.DieticianProfileDAO;
import com.wellbeeing.wellbeeing.repository.account.DoctorProfileDAO;
import com.wellbeeing.wellbeeing.repository.account.TrainerProfileDAO;
import com.wellbeeing.wellbeeing.repository.account.UserDAO;
import com.wellbeeing.wellbeeing.repository.social.RoleRequestDAO;
import com.wellbeeing.wellbeeing.domain.exception.NotFoundException;
import com.wellbeeing.wellbeeing.service.account.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("roleRequestService")
public class RoleRequestServiceImpl implements RoleRequestService {
    private final RoleRequestDAO roleRequestDAO;
    private final UserDAO userDAO;
    private final UserService userService;
    private final TrainerProfileDAO trainerProfileDAO;
    private final DoctorProfileDAO doctorProfileDAO;
    private final DieticianProfileDAO dieticianProfileDAO;

    Set<String> possibleRoles;

    public RoleRequestServiceImpl(@Qualifier("roleRequestDAO") RoleRequestDAO roleRequestDAO,
                                  @Qualifier("userDAO") UserDAO userDAO, UserService userService,
                                  @Qualifier("trainerProfileDAO") TrainerProfileDAO trainerProfileDAO,
                                  @Qualifier("doctorProfileDAO") DoctorProfileDAO doctorProfileDAO,
                                  @Qualifier("dieticianProfileDAO") DieticianProfileDAO dieticianProfileDAO) {
        this.roleRequestDAO = roleRequestDAO;
        this.userDAO = userDAO;
        this.userService = userService;
        this.trainerProfileDAO = trainerProfileDAO;
        this.doctorProfileDAO = doctorProfileDAO;
        this.dieticianProfileDAO = dieticianProfileDAO;
        this.possibleRoles = new HashSet<>();
        this.possibleRoles.add("ROLE_DOCTOR");
        this.possibleRoles.add("ROLE_DIETICIAN");
        this.possibleRoles.add("ROLE_TRAINER");
    }


    @Override
    public Page<RoleRequest> getAllRoleRequests(Specification<RoleRequest> reqSpec, Pageable pageable) {
        return roleRequestDAO.findAll(reqSpec,pageable);
    }

    @Override
    public RoleRequest getRoleRequest(long roleRequestId) {
        return roleRequestDAO.findById(roleRequestId).orElse(null);
    }

    @Override
    public List<RoleRequest> getMyRoleRequests(String ownerName) {
        return roleRequestDAO.findRoleRequestsBySubmitterProfileUserEmailOrderByAddedDateDesc(ownerName);
    }

    @Override
    public RoleRequest submitRoleRequest(RoleRequest roleRequest, String submitterName) throws NotFoundException, ForbiddenException {
        User user = userDAO.findUserByEmail(submitterName).orElse(null);
        Set<Role> userRoles = user.getRoles();

        roleRequest.setSubmitter(user.getProfile());

        if (roleRequest.getRole() == null)
        {
            throw new NotFoundException("You have to choose a role!");
        }

        for(Role role: userRoles) {
            if (role.getRole().name().equals(roleRequest.getRole().toString())) {
                throw new ForbiddenException("You already have that role");
            }
        }

        if (roleRequest.getDocumentImgPath() == null || Objects.equals(roleRequest.getDocumentImgPath(), ""))
        {
            throw new NotFoundException("You have to attach the document confirming your specialisation!");
        }

        for(String role: possibleRoles) {
            if (role.equals(roleRequest.getRole().toString())) {
                roleRequestDAO.save(roleRequest);
                break;
            }
        }

        return roleRequest;
    }

    @Override
    public RoleRequest updateRoleRequest(RoleRequest roleRequest, String updaterName) throws NotFoundException, ForbiddenException {
        RoleRequest targetRoleRequest = roleRequestDAO.findById(roleRequest.getRoleReqId()).orElse(null);
        if (targetRoleRequest == null)
            throw new NotFoundException(String.format("There's no role request with id=%d", roleRequest.getRoleReqId()));

        if (targetRoleRequest.getStatus() != EStatus.PENDING)
            throw new ForbiddenException("Selected request is not pending!");

        Profile updaterProfile = userDAO.findUserByEmail(updaterName).orElse(null).getProfile();
        Profile submitterProfile = targetRoleRequest.getSubmitter();

        if (updaterProfile != submitterProfile)
            throw new ForbiddenException("You do not have access to this role request!");

        roleRequest.setAddedDate(new Date());
        roleRequest.setStatus(EStatus.PENDING);
        roleRequest.setSubmitter(submitterProfile);
        roleRequest.setComment("");

        Set<Role> userRoles = submitterProfile.getRoles();

        for(Role role: userRoles) {
            if (role.getRole().equals(roleRequest.getRole())) {
                throw new ForbiddenException("You already have that role");
            }
        }

        for(String role: possibleRoles) {
            if (role.equals(roleRequest.getRole().toString())) {
                roleRequestDAO.save(roleRequest);
                break;
            }
        }

        return roleRequest;
    }

    @Override
    public boolean processRoleRequest(long roleReqId, RoleRequest roleRequest) throws NotFoundException, IllegalArgumentException {
        RoleRequest targetRoleRequest = roleRequestDAO.findById(roleReqId).orElse(null);
        if (targetRoleRequest == null)
            throw new NotFoundException(String.format("There's no role request with id=%d", roleRequest.getRoleReqId()));

        if (Objects.equals(roleRequest.getStatus().toString(), EStatus.ACCEPTED.toString())) {
            System.out.println("jestem w ifie");
            Profile submitter = targetRoleRequest.getSubmitter();
            String userMail = submitter.getProfileUser().getEmail();
            userService.addRoleToUser(userMail, targetRoleRequest.getRole().toString());
            System.out.println("dodaje role " + targetRoleRequest.getRole().toString() + " do " + userMail);
            targetRoleRequest.setStatus(EStatus.ACCEPTED);
            targetRoleRequest.setComment(roleRequest.getComment());
            roleRequestDAO.save(targetRoleRequest);

            List<RoleRequest> reqsToCancel = roleRequestDAO.findRoleRequestsBySubmitterProfileUserEmailAndRoleAndStatus(userMail, targetRoleRequest.getRole(), EStatus.PENDING);
            reqsToCancel.forEach(r -> {
                System.out.println("anuluje inne");
                r.setStatus(EStatus.CANCELLED);
                roleRequestDAO.save(r);
            });

            switch(targetRoleRequest.getRole().toString()) {
                case ERole.Name.ROLE_TRAINER:
                    TrainerProfile newTrainer = new TrainerProfile(submitter);
                    trainerProfileDAO.save(newTrainer);
                    System.out.println("dodaje trenera profil");
                    break;
                case ERole.Name.ROLE_DOCTOR:
                    DoctorProfile newDoctor = new DoctorProfile(submitter);
                    doctorProfileDAO.save(newDoctor);
                    System.out.println("dodaje doktora profil");
                    break;
                case ERole.Name.ROLE_DIETICIAN:
                    DieticianProfile newDietician = new DieticianProfile(submitter);
                    dieticianProfileDAO.save(newDietician);
                    System.out.println("dodaje dietetyka profil");
                    break;
                default:
                    break;
            }
        }
        else if (Objects.equals(roleRequest.getStatus().toString(), EStatus.REJECTED.toString())) {
            targetRoleRequest.setStatus(EStatus.REJECTED);
            targetRoleRequest.setComment(roleRequest.getComment());
            roleRequestDAO.save(targetRoleRequest);
        }
        else
            throw new IllegalArgumentException("Wrong status!");


        return true;
    }

    @Override
    public boolean cancelRoleRequest(long roleRequestId, String cancellerName) throws NotFoundException, ForbiddenException {
        RoleRequest targetRoleRequest = roleRequestDAO.findById(roleRequestId).orElse(null);
        if (targetRoleRequest == null)
            throw new NotFoundException(String.format("There's no role request with id=%d", roleRequestId));

        Profile updaterProfile = userDAO.findUserByEmail(cancellerName).orElse(null).getProfile();
        Profile submitterProfile = targetRoleRequest.getSubmitter();

        if (updaterProfile != submitterProfile)
            throw new ForbiddenException("You do not have access to this role request!");

        if (!Objects.equals(targetRoleRequest.getStatus().toString(), EStatus.PENDING.toString()))
            throw new ForbiddenException("You can't cancel this request!");

        targetRoleRequest.setStatus(EStatus.CANCELLED);
        roleRequestDAO.save(targetRoleRequest);
        return true;
    }
}
