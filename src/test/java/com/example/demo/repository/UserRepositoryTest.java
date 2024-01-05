package com.example.demo.repository;

import com.example.demo.model.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource("classpath:test-application.properties")
@Sql("/sql/user-repository-test-data.sql")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByIdAndStatus_로_유저_데이터를_찾아올_수_있다() {
        //given

        //when
        Optional<UserEntity> result = userRepository.findByIdAndStatus(1L,UserStatus.ACTIVE);

        //then
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    void findByIdAndStatus_는_데이터가_없으면_Optional_Empty_를_내려준다() {
        //given

        //when
        Optional<UserEntity> result = userRepository.findByIdAndStatus(1L,UserStatus.PENDING);

        //then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findByEmailAndStatus_로_유저_데이터를_찾아올_수_있다() {
        //given

        //when
        Optional<UserEntity> result = userRepository.findByEmailAndStatus("skehdxhd@gachon.ac.kr",UserStatus.ACTIVE);

        //then
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    void findByEmailAndStatus_는_데이터가_없으면_Optional_Empty_를_내려준다() {
        //given

        //when
        Optional<UserEntity> result = userRepository.findByEmailAndStatus("skehdxhd@gachon.ac.kr",UserStatus.PENDING);

        //then
        assertThat(result.isEmpty()).isTrue();
    }
}
