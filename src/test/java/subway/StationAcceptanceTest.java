package subway;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@DisplayName("지하철역 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationAcceptanceTest {

    private static ExtractableResponse<Response> 지하철역_목록_조회() {
        return RestAssured.given().log().all()
                .when().get("/stations")
                .then().log().all()
                .extract();
    }

    private static ExtractableResponse<Response> 지하철역을_생성한다(String 역이름) {
        Map<String, String> params = new HashMap<>();
        params.put("name", 역이름);
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/stations")
                .then().log().all()
                .extract();
    }

    private static ExtractableResponse<Response> 지할척역_제거한다(int stationId) {
        return RestAssured.given().log().all()
                .when().delete("/stations/" + stationId)
                .then().log().all()
                .extract();
    }

    private static ExtractableResponse<Response> 지하철노선을_조회한다(int id) {
        return RestAssured.given().log().all()
                .when().get("/stations/" + id)
                .then().log().all()
                .extract();
    }

    private static ExtractableResponse<Response> 지하철노선_수정(int 수정할_자하철역_아이디, String 수정네이밍) {
        Map<String, String> params = new HashMap<>();
        params.put("name", 수정네이밍);
        return RestAssured
                .given().log().all().body(params).contentType(ContentType.JSON)
                .when().put("/stations/" + 수정할_자하철역_아이디)
                .then().log().all()
                .extract();
    }

    private static List<String> 지하철역_목록의_이름을_파싱한다(ExtractableResponse<Response> 지하철역_목록_조회_결과) {
        return 지하철역_목록_조회_결과.jsonPath().getList("name", String.class);
    }

    private static int 지하철역의_아이디를_파싱한다(ExtractableResponse<Response> 지하철역_조회_결과) {
        return 지하철역_조회_결과.jsonPath().getInt("id");
    }

    private static List<Integer> 지하철역_목록에서_아이디를_파싱한다(ExtractableResponse<Response> 지하철역_목록_조회_결과) {
        return 지하철역_목록_조회_결과.jsonPath().getList("id", Integer.class);
    }

    private static String 지하철역의_이름을_파싱한다(ExtractableResponse<Response> 지하철역_조회_결과) {
        return 지하철역_조회_결과.jsonPath().getString("name");
    }

    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        var 강남역 = 지하철역을_생성한다("강남역");

        // then
        assertThat(강남역.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // then
        List<String> 지하철역_목록_이름 = 지하철역_목록의_이름을_파싱한다(지하철역_목록_조회());
        assertThat(지하철역_목록_이름).containsAnyOf("강남역");
    }


    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @DisplayName("지하철역 목록 조회")
    @Test
    void searchSubwayStationList() {
        // given
        지하철역을_생성한다("강남역");
        지하철역을_생성한다("이수역");

        // when
        List<String> 지하철역_목록_이름 = 지하철역_목록의_이름을_파싱한다(지하철역_목록_조회());

        // then
        assertThat(지하철역_목록_이름).contains("강남역", "이수역");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철노선 조회")
    @Test
    void searchSubwayStation() {
        // given
        var 강남역 = 지하철역을_생성한다("강남역");

        // when
        var 조회된_강남역 = 지하철노선을_조회한다(지하철역의_아이디를_파싱한다(강남역));

        // Then
        assertThat(지하철역의_이름을_파싱한다(조회된_강남역)).isEqualTo("강남역");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @DisplayName("지하철노선 수정")
    @Test
    void updateSubwayStation() {
        // given
        var 강남역 = 지하철역을_생성한다("강남역");
        int 수정할_지하철역의_아이디 = 지하철역의_아이디를_파싱한다(강남역);


        // when
        var 수정후_지하철역 = 지하철노선_수정(수정할_지하철역의_아이디, "영등포역");

        // Then
        assertThat(지하철역의_이름을_파싱한다(수정후_지하철역)).isEqualTo("영등포역");
        assertThat(지하철역의_이름을_파싱한다(지하철노선을_조회한다(수정할_지하철역의_아이디))).isEqualTo("영등포역");
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @DisplayName("지하철역 제거")
    @Test
    public void removeSubwayStation() {
        // given
        var 강남역 = 지하철역을_생성한다("강남역");
        int 강남역_아이디 = 지하철역의_아이디를_파싱한다(강남역);

        // when
        지할척역_제거한다(강남역_아이디);

        // then
        List<Integer> 지할철역_목록_아이디 = 지하철역_목록에서_아이디를_파싱한다(지하철역_목록_조회());
        assertThat(지할철역_목록_아이디).doesNotContain(강남역_아이디);
    }

}
