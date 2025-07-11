package com.example.myShop.repository;

import com.example.myShop.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.myShop.entity.Item;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>
, ItemRepositoryCustom {
    List<Item> findByItemName(String itemName);
    List<Item> findByItemNameOrItemDetail(String itemName, String itemDetail);
    List<Item> findByPriceLessThan(Integer price);
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);
}