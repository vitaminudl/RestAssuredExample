package tests.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.fakeapiusers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import utils.CustomTpl;

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class SimpleApiRefactoredTests {
    @BeforeAll
    public static void setUp(){
        RestAssured.baseURI = "https://fakestoreapi.com";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(),
                CustomTpl.customLogFilter().withCustomTemplates());

    }

    @Test
    public void getAllUsersTest(){
        given().get("/users")
                .then()
                .statusCode(200);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    public void getSingleUserTest(int userId){
        UserRoot response = given()
                .pathParam("userId", userId)
                .get("/users/{userId}")
                .then()
                .statusCode(200)
                .extract().as(UserRoot.class);
        Assertions.assertEquals(userId, response.getId());
        Assertions.assertTrue(response.getAddress().getZipcode().matches("\\d{5}-\\d{4}"));
    }


    @ParameterizedTest
    @ValueSource(ints = {1, 10,})
    public void getAllUsersWithLimitTest(int limitSize){
        List<UserRoot> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("", UserRoot.class);
        Assertions.assertEquals(limitSize,users.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 40})
    public void getAllUsersWithLimitTestErrorParams(int limitSize){
        List<UserRoot> users = given()
                .queryParam("limit", limitSize)
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("", UserRoot.class);
        Assertions.assertNotEquals(limitSize,users.size());

    }

    @Test
    public void getAllUsersSortByDescTest(){
        String sortType ="desc";
        List<UserRoot> usersSorted = given()
                .queryParam("sort", sortType)
                .get("/users")
                .then()
                .extract()
                .jsonPath().getList("", UserRoot.class);

        List<UserRoot> usersNotSorted = given()
                .get("/users")
                .then()
                .extract()
                .jsonPath().getList("", UserRoot.class);

        List<Integer> sortedResponceId = usersSorted.stream()
                .map(UserRoot::getId)
                .collect(Collectors.toList());

        List<Integer> sortedByCode = usersNotSorted.stream()
                .map(x->x.getId())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        Assertions.assertNotEquals(usersSorted,usersNotSorted);
        Assertions.assertEquals(sortedResponceId, sortedByCode);
    }

    @Test
    public void addNewUserTest(){
        UserRoot user = getTestUser();

        Integer userId = given().body(user)
                .post("/users")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getInt("id");

        Assertions.assertNotNull(userId);

    }

    private UserRoot getTestUser(){
        Random random = new Random();
        Name name = new Name("Tomas","Anderson");
        Geolocation geolocation = new Geolocation("30.24788","-20.545419");

        Address address = Address.builder()
                .city("kilcoole")
                .street("7835 new road")
                .number(random.nextInt(100))
                .zipcode("12926-3874")
                .geolocation(geolocation).build();

        return UserRoot.builder()
                .name(name)
                .phone("793564532")
                .email("fakemail.cpm")
                .password("mycoolpassword")
                .address(address).build();
    }

    @Test
    public void updateUserTest(){
        UserRoot user = getTestUser();
        String oldPassword = user.getPassword();

        user.setPassword("newpass111");
        user.setEmail("fakemail@gmail.com");

        UserRoot updateUser = given()
                .body(user)
                .pathParam("userId", user.getId())
                .put("/users/{userId}")
                .then()
                .extract().as(UserRoot.class);

        Assertions.assertNotEquals(updateUser.getPassword(),oldPassword);
        //нет возможности добавить проверку старых и новых значений пароля и емейла, так как в ответе api пустое body

    }

    @Test
    public void authUserTest(){
        AuthData authData = new AuthData("jimmie_k","klein*#%*");

        String token = given().contentType(ContentType.JSON)
                .body(authData)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);
    }
}
