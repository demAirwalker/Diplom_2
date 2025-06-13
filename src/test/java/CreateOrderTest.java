import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @Description("Создание и логин нового пользователя, создание заказа для пользователя — должен вернуться 200")
    public void testCreateOrderWithUser() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("login/valid_user.json");
        JSONObject body1 = JsonUtils.readJsonFromFile("order/valid_order.json");

        // Отправляем POST-запрос на регистрацию
        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/register");

        // Логин пользователя через auth/login
        Response response = given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/login");

        // Извлекаем accessToken
        String accessToken = response.jsonPath().getString("accessToken");

        // Создание заказа
        Response response1 = given()
                .contentType(ContentType.JSON)
                .body(body1.toString())
                .header("Authorization", accessToken)
                .when()
                .post("/api/orders");

        // Проверка кода и базовых значений
        response1.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.owner.email", equalTo(body.getString("email").toLowerCase()))
                .body("order.owner.name", equalTo(body.getString("name")))
                .body("order._id", notNullValue());

        // Удаляем пользователя через DELETE /api/auth/user
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202); // 202 — ожидаемый статус удаления
    }

    @Test
    @Description("Создание заказа без пользователя — должен вернуться 200")
    public void testCreateOrderWithoutUser() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("order/valid_order.json");

        // Создание заказа
        Response response = given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/orders");

        // Проверка кода и базовых значений
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @Description("Создание пустого заказа — должен вернуться 400")
    public void testCreateEmptyOrder() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("order/empty_order.json");

        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Description("Создание заказа с некорректными ингредиентами — должен вернуться 400")
    public void testCreateInvalidOrder() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("order/invalid_order.json");

        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("One or more ids provided are incorrect"));
    }

}
