package com.wellbeeing.wellbeeing.api.sport;

import com.wellbeeing.wellbeeing.domain.account.ERole;
import com.wellbeeing.wellbeeing.domain.PaginatedResponse;
import com.wellbeeing.wellbeeing.domain.exception.NotFoundException;
import com.wellbeeing.wellbeeing.domain.message.sport.AddExerciseWithLabelsRequest;
import com.wellbeeing.wellbeeing.domain.sport.EExerciseType;
import com.wellbeeing.wellbeeing.domain.sport.Exercise;
import com.wellbeeing.wellbeeing.repository.account.UserDAO;
import com.wellbeeing.wellbeeing.service.sport.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping(path = "/sport/exercise")
@RestController
public class ExerciseController {
    @Autowired
    private AuthenticationManager authenticationManager;
    private ExerciseService exerciseService;
    private UserDAO userDAO;


    public ExerciseController(@Qualifier("exerciseService") ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @RequestMapping(path = "/{id}")
    public ResponseEntity<?> getExerciseId(@PathVariable(value = "id") Long exerciseId) {
        return new ResponseEntity<>(exerciseService.getExercise(exerciseId), HttpStatus.OK);
    }

//    @GetMapping(path = "")
//    public ResponseEntity<?> getExercises() {
//        return new ResponseEntity<>(exerciseService.getAllExercises(), HttpStatus.OK);
//    }

    @GetMapping(path = "")
    public ResponseEntity<?> getExercisesPaginated(@RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "3") int size) {
            List<Exercise> exercises;
            Pageable paging = PageRequest.of(page, size);

            Page<Exercise> pageExercises;
            pageExercises = exerciseService.getAllExercises(paging);
            exercises = pageExercises.getContent();

        PaginatedResponse response = PaginatedResponse.builder()
                        .objects(exercises)
                        .currentPage(pageExercises.getNumber())
                        .totalItems(pageExercises.getTotalElements())
                        .totalPages(pageExercises.getTotalPages()).build();
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "")
    @RolesAllowed(ERole.Name.ROLE_TRAINER)
    public ResponseEntity<?> addExercise(@RequestBody @NonNull AddExerciseWithLabelsRequest request, Principal principal) throws NotFoundException {
        Exercise createdExercise;
            createdExercise = exerciseService.addExercise(request.getExercise(), principal.getName());
            request.getLabelsIds().forEach(labelId -> {
                try {
                    exerciseService.addLabelToExerciseByLabelId(createdExercise.getExerciseId(), labelId);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }
            });
        if (createdExercise == null) {
            return new ResponseEntity<>("Couldn't create exercise!", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(createdExercise, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable(value = "id") Long exerciseId) throws NotFoundException {
        exerciseService.deleteExercise(exerciseId);
        return new ResponseEntity<>("Successfully deleted exercise with id=" + exerciseId, HttpStatus.OK);
    }

    @RequestMapping("/{id}/label-to-exercise-name/{labelId}")
    public ResponseEntity<?> addLabelToExerciseByLabelId(@PathVariable(value = "id") Long exerciseId, @PathVariable(value = "labelId") Long labelId) throws NotFoundException {
        exerciseService.addLabelToExerciseByLabelId(exerciseId, labelId);
        Exercise updatedExercise = exerciseService.getExercise(exerciseId);
        return new ResponseEntity<>(updatedExercise, HttpStatus.OK);
    }

    @RequestMapping("/{id}/label-to-exercise-name/{labelName}")
    public ResponseEntity<?> addLabelToExerciseByLabelName(@PathVariable(value = "id") Long exerciseId, @PathVariable(value = "labelName") String labelName) throws NotFoundException {
        exerciseService.addLabelToExerciseByLabelName(exerciseId, labelName);
        Exercise updatedExercise = exerciseService.getExercise(exerciseId);
        return new ResponseEntity<>(updatedExercise, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExercise(@PathVariable(value = "id") Long exerciseId, @RequestBody @NonNull Exercise exercise) throws NotFoundException {
        exercise.setExerciseId(exerciseId);
        exerciseService.updateExercise(exercise);
        return new ResponseEntity<>(exercise, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> partialUpdateExercise(@PathVariable(value = "id") Long exerciseId, @RequestBody Map<String, Object> fields) throws Exception {
        // Sanitize and validate the data
        if (exerciseId <= 0 || fields == null || fields.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Invalid claim object received or invalid id or id does not match object
        }

        Exercise exercise = exerciseService.getExercise(exerciseId);

        // Does the object exist?
        if (exercise == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Claim object does not exist
        }

        // Remove id from request, we don't ever want to change the id.
        // This is not necessary, you can just do it to save time on the reflection
        // loop used below since we checked the id above
        fields.remove("id");
        fields.forEach((k, v) -> {
            // use reflection to get field k on object and set it to value v
            // Change Claim.class to whatver your object is: Object.class
            Field field = ReflectionUtils.findField(Exercise.class, k); // find field in the object class
            field.setAccessible(true);
            if (field.getType() == EExerciseType.class)
                v = EExerciseType.valueOf((String) v);
            ReflectionUtils.setField(field, exercise, v); // set given field for defined object to value V
        });

        Exercise updated = exerciseService.partialUpdateExercise(exercise);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }
}
