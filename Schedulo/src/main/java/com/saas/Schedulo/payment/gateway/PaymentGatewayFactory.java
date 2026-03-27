package com.saas.Schedulo.payment.gateway;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayFactory(List<PaymentGateway> gatewayList) {
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(
                        PaymentGateway::getName,
                        Function.identity()
                ));
    }

    public PaymentGateway getGateway(String name) {
        PaymentGateway gateway = gateways.get(name.toLowerCase());
        if (gateway == null) {
            throw new IllegalArgumentException("Unknown payment gateway: " + name);
        }
        return gateway;
    }

    public PaymentGateway getDefaultGateway() {
        return getGateway("stripe");
    }
}

