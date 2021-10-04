package project.pr.repository.db.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.util.StringUtils;
import project.pr.domain.Order;
import project.pr.domain.OrderDomainSearch;
import project.pr.domain.QItem;
import project.pr.domain.QOrderItem;
import project.pr.domain.status.OrderStatus;
import project.pr.repository.db.OrderRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

import static project.pr.domain.QMember.member;
import static project.pr.domain.QOrder.order;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    EntityManager em;

    public OrderRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Order> findSearchOrderMember(String name , OrderStatus orderStatus) {

        List<Order> orders = queryFactory.select(order).from(order).join(order.member, member)
                .where(nameEq(name),
                        orderStatusEq(orderStatus))
                .fetch();
        return orders;
    }


    private BooleanExpression orderStatusEq(OrderStatus orderStatus) {
        BooleanExpression ex = orderStatus != null ? order.status.eq(orderStatus) : null;
        return ex;
    }

    private BooleanExpression nameEq(String name) {
        BooleanExpression ex = StringUtils.hasText(name) ? member.name.eq(name) : null;
        return ex;
    }


    public List<Order> findAllQuaryDsl(Long orderId){
        List<Order> fetch = queryFactory.select(order).from(order)
                .leftJoin(order.member, member)
                .fetchJoin()
                .leftJoin(order.orderItems, QOrderItem.orderItem)
                .fetchJoin()
                .leftJoin(QOrderItem.orderItem.item , QItem.item)
                .fetchJoin()
                .fetch();
        return fetch;
    }

    @Override
    public List<Order> findOrderSearch(OrderDomainSearch OrderDomainSearch) {

        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (OrderDomainSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(OrderDomainSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (OrderDomainSearch.getOrderStatus() != null) {
            query = query.setParameter("status", OrderDomainSearch.getOrderStatus());
        }
        if (StringUtils.hasText(OrderDomainSearch.getMemberName())) {
            query = query.setParameter("name", OrderDomainSearch.getMemberName());
        }
        return query.getResultList();
    }
}
