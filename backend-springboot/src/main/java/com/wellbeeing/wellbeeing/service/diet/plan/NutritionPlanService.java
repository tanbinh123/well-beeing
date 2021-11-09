package com.wellbeeing.wellbeeing.service.diet.plan;

import com.wellbeeing.wellbeeing.domain.diet.NutritionPlan;
import com.wellbeeing.wellbeeing.domain.diet.NutritionPlanPosition;
import com.wellbeeing.wellbeeing.domain.exception.NotFoundException;
import com.wellbeeing.wellbeeing.domain.exception.NutritionPlanGenerationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("nutritionPlanService")
public interface NutritionPlanService {
    boolean deleteNutritionPlanFromProfile(UUID nutritionPlanId) throws NotFoundException;
    List<NutritionPlan> getAllProfileNutritionPlans(UUID profileId) throws NotFoundException;
    NutritionPlan getNutritionPlanById(UUID nutritionPlanId) throws NotFoundException;
    NutritionPlan generateNutritionPlanForProfile(UUID profileId) throws NotFoundException, NutritionPlanGenerationException;
    NutritionPlan addEmptyNutritionPlanToProfile(UUID profileId) throws NotFoundException;
    NutritionPlan addNutritionPlanToProfile(NutritionPlan nutritionPlan, UUID profileId) throws NotFoundException;
    NutritionPlan addPositionToProfileNutritionPlan(NutritionPlanPosition position, UUID nutritionPlanId) throws NotFoundException;
    NutritionPlan updatePositionInProfileNutritionPlan(NutritionPlanPosition position, UUID nutritionPlanId) throws NotFoundException;
    NutritionPlan deletePositionFromProfileNutritionPlan(UUID positionId, UUID nutritionPlanId) throws NotFoundException;
}