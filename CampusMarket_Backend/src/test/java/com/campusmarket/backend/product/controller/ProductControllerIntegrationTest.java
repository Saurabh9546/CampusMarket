package com.campusmarket.backend.product.controller;

import com.campusmarket.backend.college.entity.College;
import com.campusmarket.backend.exception.ForbiddenException;
import com.campusmarket.backend.exception.ResourceNotFoundException;
import com.campusmarket.backend.product.dto.ProductCreateRequest;
import com.campusmarket.backend.product.dto.ProductDto;
import com.campusmarket.backend.product.entity.ProductStatus;
import com.campusmarket.backend.product.service.ProductService;
import com.campusmarket.backend.user.entity.User;
import com.campusmarket.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.test.context.SpringBootTest
class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private static final Long USER_ID = 1L;
    private static final Long COLLEGE_ID = 10L;
    private static final Long PRODUCT_ID = 100L;

    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        College college = mock(College.class);
        when(college.getId()).thenReturn(COLLEGE_ID);

        User user = new User();
        user.setId(USER_ID);
        user.setName("Test User");
        user.setCollege(college);
        lenient().when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        createRequest = new ProductCreateRequest();
        createRequest.setTitle("Used Textbook");
        createRequest.setDescription("Barely used, good condition");
        createRequest.setPrice(250.0);
        createRequest.setCategory("Books");
    }

    private ProductDto sampleDto() {
        return new ProductDto(PRODUCT_ID, "Used Textbook", "Barely used, good condition",
                250.0, "Books", ProductStatus.AVAILABLE, USER_ID, LocalDateTime.now(), null);
    }

    // ---------- GET /products (browse) ----------

    @Test
    @WithMockUser(username = "1")
    void browse_returns200_withPagedResults() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleDto()), PageRequest.of(0, 20), 1);
        when(productService.browse(eq(COLLEGE_ID), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Used Textbook"));
    }

    @Test
    void browse_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- GET /products/mine ----------

    @Test
    @WithMockUser(username = "1")
    void mine_returns200_withSellersOwnListings() throws Exception {
        Page<ProductDto> page = new PageImpl<>(List.of(sampleDto()));
        when(productService.getMine(eq(USER_ID), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].sellerId").value(USER_ID));
    }

    // ---------- GET /products/{id} ----------

    @Test
    @WithMockUser(username = "1")
    void getById_returns200_whenFound() throws Exception {
        when(productService.getById(PRODUCT_ID, COLLEGE_ID)).thenReturn(sampleDto());

        mockMvc.perform(get("/api/v1/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(PRODUCT_ID));
    }

    @Test
    @WithMockUser(username = "1")
    void getById_returns404_whenNotFoundOrWrongCollege() throws Exception {
        when(productService.getById(PRODUCT_ID, COLLEGE_ID))
                .thenThrow(new ResourceNotFoundException("Listing not found"));

        mockMvc.perform(get("/api/v1/products/{id}", PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    // ---------- POST /products ----------

    @Test
    @WithMockUser(username = "1")
    void create_returns200_whenValid() throws Exception {
        when(productService.create(any(ProductCreateRequest.class), eq(USER_ID), eq(COLLEGE_ID)))
                .thenReturn(sampleDto());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Used Textbook"));
    }

    @Test
    @WithMockUser(username = "1")
    void create_returns400_whenPriceIsNegative() throws Exception {
        createRequest.setPrice(-10.0);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(productService, never()).create(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "1")
    void create_returns400_whenTitleIsBlank() throws Exception {
        createRequest.setTitle("");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    // ---------- PUT /products/{id} ----------

    @Test
    @WithMockUser(username = "1")
    void update_returns200_whenOwner() throws Exception {
        when(productService.update(eq(PRODUCT_ID), eq(USER_ID), any(ProductCreateRequest.class)))
                .thenReturn(sampleDto());

        mockMvc.perform(put("/api/v1/products/{id}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(PRODUCT_ID));
    }

    @Test
    @WithMockUser(username = "1")
    void update_returns403_whenNotOwner() throws Exception {
        when(productService.update(eq(PRODUCT_ID), eq(USER_ID), any(ProductCreateRequest.class)))
                .thenThrow(new ForbiddenException("FORBIDDEN_NOT_OWNER"));

        mockMvc.perform(put("/api/v1/products/{id}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN_NOT_OWNER"));
    }

    // ---------- PATCH /products/{id}/sold ----------

    @Test
    @WithMockUser(username = "1")
    void markSold_returns200_whenOwner() throws Exception {
        when(productService.markSold(PRODUCT_ID, USER_ID)).thenReturn(sampleDto());

        mockMvc.perform(patch("/api/v1/products/{id}/sold", PRODUCT_ID))
                .andExpect(status().isOk());

        verify(productService).markSold(PRODUCT_ID, USER_ID);
    }

    @Test
    @WithMockUser(username = "1")
    void markSold_returns409_whenStaleVersionConflict() throws Exception {
        when(productService.markSold(PRODUCT_ID, USER_ID))
                .thenThrow(new ObjectOptimisticLockingFailureException("Product", PRODUCT_ID));

        mockMvc.perform(patch("/api/v1/products/{id}/sold", PRODUCT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("STALE_UPDATE"));
    }

    // ---------- DELETE /products/{id} ----------

    @Test
    @WithMockUser(username = "1")
    void delete_returns200_whenOwner() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(productService).delete(PRODUCT_ID, USER_ID);
    }

    @Test
    @WithMockUser(username = "1")
    void delete_returns403_whenNotOwner() throws Exception {
        doThrow(new ForbiddenException("FORBIDDEN_NOT_OWNER"))
                .when(productService).delete(PRODUCT_ID, USER_ID);

        mockMvc.perform(delete("/api/v1/products/{id}", PRODUCT_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN_NOT_OWNER"));
    }
}