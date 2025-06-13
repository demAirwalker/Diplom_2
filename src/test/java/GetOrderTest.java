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

public class GetOrderTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @Description("Создание и логин нового пользователя, создание заказа и получение списка заказов для пользователя, — должен вернуться 200")
    public void testGetOrderWithUser() throws Exception {
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
        given()
                .contentType(ContentType.JSON)
                .body(body1.toString())
                .header("Authorization", accessToken)
                .when()
                .post("/api/orders");

        // Получения списка заказов
        Response response1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .when()
                .get("/api/orders");

        // Проверка кода и базовых значений
        response1.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());

        // Удаляем пользователя через DELETE /api/auth/user
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202); // 202 — ожидаемый статус удаления
    }

    @Test
    @Description("Получение списка заказов без авторизации — должен вернуться 401")
    public void testCreateInvalidOrder() throws Exception {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/orders")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

}
