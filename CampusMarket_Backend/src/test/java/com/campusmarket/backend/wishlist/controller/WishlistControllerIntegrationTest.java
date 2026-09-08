package com.campusmarket.backend.wishlist.controller;

import com.campusmarket.backend.exception.ConflictException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.ProductStatus;
import com.campusmarket.backend.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class WishlistControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private WishlistService wishlistService;

    private MockMvc mockMvc;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    private ProductDto sampleDto() {
        return new ProductDto(PRODUCT_ID, "Used Textbook", "Barely used, good condition",
                250.0, "Books", ProductStatus.AVAILABLE, 2L, LocalDateTime.now(), null);
    }

    // ---------- GET /wishlist ----------

    @Test
    @WithMockUser(username = "1")
    void list_returns200_withWishlistedProducts() throws Exception {
        when(wishlistService.list(USER_ID)).thenReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(PRODUCT_ID));
    }

    @Test
    void list_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- POST /wishlist/{productId} ----------

    @Test
    @WithMockUser(username = "1")
    void add_returns200_whenValid() throws Exception {
        mockMvc.perform(post("/api/v1/wishlist/{productId}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(wishlistService).add(USER_ID, PRODUCT_ID);
    }

    @Test
    @WithMockUser(username = "1")
    void add_returns404_whenProductNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Listing not found"))
                .when(wishlistService).add(USER_ID, PRODUCT_ID);

        mockMvc.perform(post("/api/v1/wishlist/{productId}", PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "1")
    void add_returns409_whenAlreadyWishlisted() throws Exception {
        doThrow(new ConflictException("Already wishlisted", "DUPLICATE_WISHLIST_ENTRY"))
                .when(wishlistService).add(USER_ID, PRODUCT_ID);

        mockMvc.perform(post("/api/v1/wishlist/{productId}", PRODUCT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_WISHLIST_ENTRY"));
    }

    // ---------- DELETE /wishlist/{productId} ----------

    @Test
    @WithMockUser(username = "1")
    void remove_returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist/{productId}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(wishlistService).remove(USER_ID, PRODUCT_ID);
    }

    @Test
    void remove_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/wishlist/{productId}", PRODUCT_ID))
                .andExpect(status().isUnauthorized());

        verify(wishlistService, never()).remove(any(), any());
    }
}