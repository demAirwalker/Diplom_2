import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpdateUserTest {

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @Test
    @Description("Создание и изменение имени нового пользователя — должен вернуться 200")
    public void testUpdateUserName() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("update/valid_user.json");
        JSONObject body1 = JsonUtils.readJsonFromFile("update/updated_user_name.json");

        // Отправляем POST-запрос на регистрацию
        Response response = given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/register");

        // Извлекаем accessToken
        String accessToken = response.jsonPath().getString("accessToken");

        // Обновление данных пользователя через PATCH /api/auth/user
        Response response1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(body1.toString())
                .when()
                .patch("/api/auth/user");

        // Проверка кода и базовых значений
        response1.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(body1.getString("email").toLowerCase()))
                .body("user.name", equalTo(body1.getString("name")));

        // Удаляем пользователя через DELETE /api/auth/user
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202); // 202 — ожидаемый статус удаления
    }

    @Test
    @Description("Создание и изменение email нового пользователя — должен вернуться 200")
    public void testUpdateUserEmail() throws Exception {
        JSONObject body = JsonUtils.readJsonFromFile("update/valid_user.json");
        JSONObject body1 = JsonUtils.readJsonFromFile("update/updated_user_email.json");

        // Отправляем POST-запрос на регистрацию
        Response response = given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/register");

        // Извлекаем accessToken
        String accessToken = response.jsonPath().getString("accessToken");

        // Обновление данных пользователя через PATCH /api/auth/user
        Response response1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(body1.toString())
                .when()
                .patch("/api/auth/user");

        // Проверка кода и базовых значений
        response1.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(body1.getString("email").toLowerCase()))
                .body("user.name", equalTo(body1.getString("name")));

        // Удаляем пользователя через DELETE /api/auth/user
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202); // 202 — ожидаемый статус удаления
    }

    @Test
    @Description("Обновление пользователя без Auth — должен вернуться 401")
    public void testUpdateUserNoAuth() throws Exception {
        JSONObject body1 = JsonUtils.readJsonFromFile("update/updated_user_name.json");

        // Обновление данных пользователя через PATCH /api/auth/user без Auth
        given()
                .contentType(ContentType.JSON)
                .body(body1.toString())
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}
