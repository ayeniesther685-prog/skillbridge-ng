package org.skillbridge.repository;

import org.junit.jupiter.api.Test;
import org.skillbridge.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesAndFindsByEmail() {
        User u = new User();
        u.setFirstName("Ada");
        u.setLastName("Obi");
        u.setEmail("ada.obi@example.com");
        u.setPassword("hashed");

        userRepository.saveAndFlush(u);

        assertThat(userRepository.findByEmailIgnoreCase("ADA.OBI@example.com")).isPresent();
        assertThat(userRepository.existsByEmailIgnoreCase("ada.obi@example.com")).isTrue();
    }
}