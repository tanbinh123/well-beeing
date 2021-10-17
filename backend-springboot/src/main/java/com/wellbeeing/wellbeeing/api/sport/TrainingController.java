package com.wellbeeing.wellbeeing.api.sport;

import com.wellbeeing.wellbeeing.domain.account.ERole;
import com.wellbeeing.wellbeeing.domain.message.ErrorMessage;
import com.wellbeeing.wellbeeing.domain.message.sport.AddExerciseToTrainingRequest;
import com.wellbeeing.wellbeeing.domain.sport.ExerciseInTraining;
import com.wellbeeing.wellbeeing.domain.sport.Training;
import com.wellbeeing.wellbeeing.domain.sport.TrainingPosition;
import com.wellbeeing.wellbeeing.repository.account.UserDAO;
import com.wellbeeing.wellbeeing.service.sport.ExerciseService;
import com.wellbeeing.wellbeeing.service.sport.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping(path = "/sport/training")
public class TrainingController {
    private TrainingService trainingService;
    private UserDAO userDAO;


    public TrainingController(@Qualifier("trainingService") TrainingService trainingService) {
        this.trainingService = trainingService;
    }
    @RequestMapping(path = "/{id}")
    public ResponseEntity<?> getTrainingById(@PathVariable(value = "id") Long trainingId) {
        return new ResponseEntity<>(trainingService.getTraining(trainingId), HttpStatus.OK);
    }

    @GetMapping(path = "")
    public ResponseEntity<?> getTrainings() {
        return new ResponseEntity<>(trainingService.getAllTrainings(), HttpStatus.OK);
    }

    @PostMapping(path = "")
    @RolesAllowed({ERole.Name.ROLE_TRAINER, ERole.Name.ROLE_BASIC_USER})
    public ResponseEntity<?> addTraining(@RequestBody @NonNull Training training, Principal principal) {
        Training createdTraining;
        try {
            createdTraining = trainingService.addTraining(training, principal.getName());
        } catch (Exception e) {
            System.out.println("Exception message: "+e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);

        }
        if (createdTraining == null) {
            return new ResponseEntity<>("Couldn't create training!", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(createdTraining, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTraining(@PathVariable(value = "id") Long trainingId ) {
        if (!trainingService.deleteTraining(trainingId)) {
            return new ResponseEntity<>(new ErrorMessage("Couldn't delete training with id=" + trainingId, "Error"), HttpStatus.OK);
        }
        return new ResponseEntity<>("Successfully deleted training with id=" + trainingId, HttpStatus.OK);
    }

    @PatchMapping("/{id}/remove-exercise/{exerciseId}")
    public ResponseEntity<?> removeExerciseFromTrainingById(@PathVariable(value = "id") Long trainingId, @PathVariable(value = "exerciseId") Long exerciseId, Principal principal) {
        if (!trainingService.removeExerciseFromTraining(trainingId, exerciseId, principal.getName())) {
            return new ResponseEntity<>(new ErrorMessage(String.format("Couldn't remove exercise %d from training with id=%d",exerciseId, trainingId), "Error"), HttpStatus.OK);
        }
        return new ResponseEntity<>(String.format("Successfully removed exercise %d from training with id=%d", exerciseId, trainingId), HttpStatus.OK);
    }

    @PatchMapping("/{id}/add-exercise/{exerciseId}")
    public ResponseEntity<?> addExerciseToTrainingById(@PathVariable(value = "id") Long trainingId, @PathVariable(value = "exerciseId") Long exerciseId, @RequestBody @NonNull AddExerciseToTrainingRequest request, Principal principal) {
        ExerciseInTraining addedExercise = trainingService.addExerciseToTraining(trainingId, exerciseId, request.getReps(),
                                                request.getTime_seconds(), request.getSeries(),
                                                principal.getName());
        if (addedExercise == null) {
            return new ResponseEntity<>(new ErrorMessage(String.format("Couldn't add exercise %d to training with id=%d",exerciseId, trainingId), "Error"), HttpStatus.OK);
        }
        return new ResponseEntity<>(addedExercise, HttpStatus.OK);
    }
    @GetMapping("/calories-burned/{trainingId}")
    public ResponseEntity<?> getCaloriesBurnedFromClient(@PathVariable(value = "trainingId") long trainingId, Principal principal) {
        double caloriesBurned = trainingService.getCaloriesBurnedFromUser(trainingId, principal.getName());
        if (caloriesBurned == -1) {
            return new ResponseEntity<>(new ErrorMessage("",""), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(caloriesBurned, HttpStatus.OK);
    }


}