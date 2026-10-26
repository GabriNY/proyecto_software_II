
package me.zhulin.shopapi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
class RepositorySmokeTest {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Test
    void shouldLoadRepository() {
        assertNotNull(productInfoRepository);
    }
}
