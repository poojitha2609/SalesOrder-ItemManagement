package org.example.demo.repository;

import org.example.demo.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    boolean existsByOrderItems_ItemNo(Long itemNo);
}

