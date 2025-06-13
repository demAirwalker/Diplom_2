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

public class LoginUserTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @Description("Создание и логин нового пользователя — должен вернуться 200")
    public void testLoginUser() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("login/valid_user.json");

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

        // Проверка кода и базовых значений
        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(body.getString("email").toLowerCase()))
                .body("user.name", equalTo(body.getString("name")))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());

        // Извлекаем accessToken
        String accessToken = response.jsonPath().getString("accessToken");

        // Удаляем пользователя через DELETE /api/auth/user
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202); // 202 — ожидаемый статус удаления
    }

    @Test
    @Description("Логин не существующего пользователя — должен вернуться 401 и сообщение")
    public void testLoginBadUser() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("login/bad_user.json");

        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
