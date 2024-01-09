package com.example.demo.service;

import com.example.demo.exception.CertificationCodeNotMatchedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.UserStatus;
import com.example.demo.model.dto.UserCreateDto;
import com.example.demo.model.dto.UserUpdateDto;
import com.example.demo.repository.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@SqlGroup({
    @Sql(value = "/sql/user-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
    @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private JavaMailSender javaMailSender;
    @Test
    void getByEmail은_ACTIVE_상태인_유저를_찾아올_수_있다() {
        //given
        String email = "skehdxhd@gachon.ac.kr";

        //when
        UserEntity result = userService.getByEmail(email);

        //then
        assertThat(result.getNickname()).isEqualTo("skehdxhd");
    }

    @Test
    void getByEmail은_PENDING_상태인_유저를_찾아올_수_없다() {
        //given
        String email = "skehdxhd2@gachon.ac.kr";

        //when / then
        assertThatThrownBy(() -> userService.getByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById는_ACTIVE_상태인_유저를_찾아올_수_있다() {
        //given

        //when
        UserEntity result = userService.getById(1L);

        //then
        assertThat(result.getNickname()).isEqualTo("skehdxhd");
    }

    @Test
    void getById는_PENDING_상태인_유저를_찾아올_수_없다() {
        //given

        //when / then
        assertThatThrownBy(() -> userService.getById(2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void userCreateDto_를_이용하여_유저를_생성할_수_있다() {
        //given
        UserCreateDto userCreateDto = UserCreateDto.builder()
                .email("test@test.com")
                .address("Gyeonggi")
                .nickname("test-k")
                .build();

        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        //when
        UserEntity result = userService.create(userCreateDto);

        //then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
//        assertThat(result.getCertificationCode()).isEqualTo("T.T");// FIXME
    }

    @Test
    void userUpdateDto_를_이용하여_유저를_수정할_수_있다() {
        //given
        UserUpdateDto userUpdateDto = UserUpdateDto.builder()
                .address("Incheon")
                .nickname("test-n")
                .build();

        //when
        userService.update(1L, userUpdateDto);

        //then
        UserEntity userEntity = userService.getById(1L);
        assertThat(userEntity.getId()).isNotNull();
        assertThat(userEntity.getNickname()).isEqualTo("test-n");
        assertThat(userEntity.getAddress()).isEqualTo("Incheon");
    }

    @Test
    void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
        //given when
        userService.login(1L);

        //then
        UserEntity userEntity = userService.getById(1L);
        assertThat(userEntity.getLastLoginAt()).isGreaterThan(0L);
//        assertThat(userEntity.getLastLoginAt()).isEqualto(); // FIXME
    }

    @Test
    void PENDING_상태의_사용자는_인증코드로_ACTIVE_상태로_활성화_시킬_수_있다() {
        //given when
        userService.verifyEmail(2L, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab");

        //then
        UserEntity userEntity = userService.getById(1L);
        assertThat(userEntity.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void PENDING_상태의_사용자는_잘못된_인증코드를_받으면_에러를_던진다() {

        //then
        assertThatThrownBy(() -> userService.verifyEmail(2L, "aaaaaaaa-aaaa-aaaa-aaaa-aasdfsdfeaaaaab"))
                .isInstanceOf(CertificationCodeNotMatchedException.class);
    }
}