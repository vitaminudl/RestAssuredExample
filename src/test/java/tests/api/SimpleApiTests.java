package tests.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import models.fakeapiusers.Address;
import models.fakeapiusers.Geolocation;
import models.fakeapiusers.Name;
import models.fakeapiusers.UserRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SimpleApiTests {

    @Test
    public void getAllUsersTest(){
        given().get("https://fakestoreapi.com/users")
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void getSingleUserTest(){
        int userId = 5;
        given().pathParam("userId", userId)
                .get("https://fakestoreapi.com/users/{userId}")
                .then().log().all()
                .body("id",equalTo(userId))
                .body("address.zipcode", matchesPattern("\\d{5}-\\d{4}"));

    }

    @Test
    public void getAllUsersWithLimitTest(){
        int limitSize = 5;
        given().queryParam("limit", limitSize)
                .get("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200)
                .body("", hasSize(limitSize));
    }

    @Test
    public void getAllUsersSortByDescTest(){
        String sortType ="desc";
        Response sortedResponsce = given().queryParam("sort", sortType)
                .get("https://fakestoreapi.com/users")
                .then().log().all()
                .extract().response();

        Response notSortedResponce = given()
                .get("https://fakestoreapi.com/users")
                .then().log().all().extract().response();

        List<Integer> sortedResponceId = sortedResponsce.jsonPath().getList("id");
        List<Integer> notSortedResponceId = notSortedResponce.jsonPath().getList("id");

        List<Integer> sortedByCode = notSortedResponceId.stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());


        Assertions.assertNotEquals(sortedResponceId,notSortedResponceId);
        Assertions.assertEquals(sortedByCode, sortedResponceId);
    }

    @Test
    public void addNewUserTest(){
        Name name = new Name("Tomas","Anderson");
        Geolocation geolocation = new Geolocation("30.24788","-20.545419");

        Address address = Address.builder()
                .city("kilcoole")
                .street("7835 new road")
                .number(3)
                .zipcode("12926-3874")
                .geolocation(geolocation).build();

        UserRoot bodyRequest = UserRoot.builder()
                .name(name)
                .phone("793564532")
                .email("fakemail.cpm")
                .password("mycoolpassword")
                .address(address).build();

        given().body(bodyRequest)
                .post("https://fakestoreapi.com/users")
                .then().log().all()
                .statusCode(200)
                .body("id", notNullValue());

    }

    private UserRoot getTestUser(){
        Name name = new Name("Tomas","Anderson");
        Geolocation geolocation = new Geolocation("30.24788","-20.545419");

        Address address = Address.builder()
                .city("kilcoole")
                .street("7835 new road")
                .number(3)
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

        given().body(user)
                .put("https://fakestoreapi.com/users/7")
                .then().log().all();
        //нет возможности добавить проверку старых и новых значений пароля и емейла, так как в ответе api пустое body

    }

    @Test
    public void deleteUserTest(){
        given().delete("https://fakestoreapi.com/users/7")
                .then().log().all()
                .statusCode(200);

    }

    @Test
    public void authUSerTest(){
        Map<String,String> userAuth = new HashMap<>();
        userAuth.put("username", "jimmie_k");
        userAuth.put("password", "klein*#%*");

        given().contentType(ContentType.JSON).body(userAuth)
                .post("https://fakestoreapi.com/auth/login")
                .then().log().all()
                .statusCode(200)
                .body("token", notNullValue());

    }

}
