package com.example.order.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.order.common.AbstractIntegrationTest;
import com.example.order.entities.Order;
import com.example.order.repositories.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired private OrderRepository orderRepository;

    private List<Order> orderList = null;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        orderList = new ArrayList<>();
        orderList.add(new Order(null, "First Order"));
        orderList.add(new Order(null, "Second Order"));
        orderList.add(new Order(null, "Third Order"));
        orderList = orderRepository.saveAll(orderList);
    }

    @Test
    void shouldFetchAllOrders() throws Exception {
        this.mockMvc
                .perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(orderList.size())));
    }

    @Test
    void shouldFindOrderById() throws Exception {
        Order order = orderList.getFirst();
        Long orderId = order.getId();

        this.mockMvc
                .perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldCreateNewOrder() throws Exception {
        Order order = new Order(null, "New Order");
        this.mockMvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderWithoutText() throws Exception {
        Order order = new Order(null, null);

        this.mockMvc
                .perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Order order = orderList.getFirst();
        order.setText("Updated Order");

        this.mockMvc
                .perform(
                        put("/api/orders/{id}", order.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Order order = orderList.getFirst();

        this.mockMvc
                .perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(order.getText())));
    }
}
