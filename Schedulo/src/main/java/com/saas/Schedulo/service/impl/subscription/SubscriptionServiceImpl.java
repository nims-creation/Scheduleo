package com.saas.Schedulo.service.impl.subscription;

import com.saas.Schedulo.dto.request.subscription.UpgradeSubscriptionRequest;
import com.saas.Schedulo.dto.response.subscription.SubscriptionPlanResponse;
import com.saas.Schedulo.dto.response.subscription.SubscriptionResponse;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.subscription.Subscription;
import com.saas.Schedulo.entity.subscription.SubscriptionPlan;
import com.saas.Schedulo.exception.resource.ResourceNotFoundException;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.payment.SubscriptionPlanRepository;
import com.saas.Schedulo.repository.payment.SubscriptionRepository;
import com.saas.Schedulo.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAvailablePlans() {
        return planRepository.findAllActiveOrdered().stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(UUID organizationId) {
        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId)
                .orElse(null);
                
        // Just return a mock FREE subscription response if they have none
        if (subscription == null) {
            SubscriptionPlan freePlan = planRepository.findByCode("FREE")
                 .orElseThrow(() -> new RuntimeException("FREE plan not configured in database"));
            return SubscriptionResponse.builder()
                 .organizationId(organizationId)
                 .plan(mapToPlanResponse(freePlan))
                 .status(Subscription.SubscriptionStatus.ACTIVE)
                 .billingCycle(Subscription.BillingCycle.MONTHLY)
                 .currentPeriodStart(LocalDateTime.now())
                 .currentPeriodEnd(LocalDateTime.now().plusYears(100))
                 .build();
        }
        
        return mapToSubscriptionResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse upgradeSubscription(UUID organizationId, UpgradeSubscriptionRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId.toString()));

        SubscriptionPlan newPlan = planRepository.findByCode(request.getPlanCode())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "code", request.getPlanCode()));

        Subscription subscription = subscriptionRepository.findByOrganizationId(organizationId).orElse(new Subscription());
        
        // Mocking checkout logic
        subscription.setOrganization(organization);
        subscription.setPlan(newPlan);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setCurrentPeriodStart(LocalDateTime.now());
        
        if (request.getBillingCycle() == Subscription.BillingCycle.MONTHLY) {
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        } else if (request.getBillingCycle() == Subscription.BillingCycle.YEARLY) {
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusYears(1));
        } else {
             subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(3));
        }
        
        subscription.setAmount(newPlan.getPriceForCycle(request.getBillingCycle()));
        subscription.setCurrency(newPlan.getCurrency());
        subscription.setNextBillingDate(subscription.getCurrentPeriodEnd());
        subscription.setAutoRenew(true);
        
        Subscription saved = subscriptionRepository.save(subscription);
        return mapToSubscriptionResponse(saved);
    }

    private SubscriptionPlanResponse mapToPlanResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .code(plan.getCode())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .quarterlyPrice(plan.getQuarterlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .planType(plan.getPlanType())
                .maxUsers(plan.getMaxUsers())
                .maxSchedulesPerDay(plan.getMaxSchedulesPerDay())
                .isPopular(plan.getIsPopular())
                .build();
    }
    
    private SubscriptionResponse mapToSubscriptionResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .organizationId(subscription.getOrganization().getId())
                .plan(mapToPlanResponse(subscription.getPlan()))
                .status(subscription.getStatus())
                .billingCycle(subscription.getBillingCycle())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .amount(subscription.getAmount())
                .autoRenew(subscription.getAutoRenew())
                .build();
    }
}
