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

public class RegisterUserTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @Description("Создание нового пользователя — должен вернуться 200")
    public void testRegisterNewUser() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("register/valid_user.json");

        // Отправляем POST-запрос на регистрацию
        Response response = given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/register");

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
    @Description("Регистрация существующего пользователя — должен вернуться 403 и сообщение")
    public void testRegisterExistingUser() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("register/existing_user.json");

        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @Description("Отсутствие обязательного поля — должен вернуться 403 и сообщение")
    public void testRegisterUserMissingName() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("register/no_name_user.json");

        given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}
