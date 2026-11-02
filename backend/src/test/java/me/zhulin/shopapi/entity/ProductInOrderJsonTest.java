
package me.zhulin.shopapi.entity;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class ProductInOrderJsonTest {

    @Test
    void deserializarProductoDelCarrito() {
        // ARRANGE
        String json = """
            {
                "productId": "F0002",
                "productName": "McDonald's Food",
                "productPrice": 20,
                "productStock": 22,
                "productDescription": "Family s",
                "categoryType": 1,
                "count": 12
            }
            """;

        JsonMapper mapper = JsonMapper.builder().build();

        // ACT
        ProductInOrder producto =
                mapper.readValue(json, ProductInOrder.class);

        // ASSERT
        assertNotNull(producto);
        assertEquals("F0002", producto.getProductId());
        assertEquals(12, producto.getCount());
        assertEquals(22, producto.getProductStock());
    }
}
