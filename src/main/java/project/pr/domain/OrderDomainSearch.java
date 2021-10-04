package project.pr.domain;

import lombok.Getter;
import lombok.Setter;
import project.pr.domain.status.OrderStatus;

@Getter @Setter
public class OrderDomainSearch {

    private String memberName;
    private OrderStatus orderStatus;

}
