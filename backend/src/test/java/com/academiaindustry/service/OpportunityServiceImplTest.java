package com.academiaindustry.service;

import com.academiaindustry.entity.Opportunity;
import com.academiaindustry.repository.OpportunityRepository;
import com.academiaindustry.repository.OpportunitySkillRequirementRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.impl.OpportunityServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OpportunityServiceImplTest {

    @Test
    void deleteRemovesSkillRequirementsBeforeOpportunity() {
        OpportunityRepository opportunityRepository = mock(OpportunityRepository.class);
        OpportunitySkillRequirementRepository requirementRepository =
                mock(OpportunitySkillRequirementRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(
                opportunityRepository, requirementRepository, mock(UserRepository.class));
        Opportunity opportunity = mock(Opportunity.class);
        when(opportunityRepository.findById(7L)).thenReturn(Optional.of(opportunity));

        service.delete(7L);

        var inOrder = inOrder(requirementRepository, opportunityRepository);
        inOrder.verify(requirementRepository).deleteByOpportunity_Id(7L);
        inOrder.verify(opportunityRepository).delete(opportunity);
    }
}
