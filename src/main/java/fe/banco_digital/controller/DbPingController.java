package fe.banco_digital.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
public class DbPingController {

	private final DataSource dataSource;

	public DbPingController(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@GetMapping("/ping")
	public ResponseEntity<?> ping() throws Exception {
		try (Connection c = dataSource.getConnection();
			 Statement st = c.createStatement();
			 ResultSet rs = st.executeQuery("select 1 as ok")) {

			rs.next();
			int ok = rs.getInt("ok");
			String url = c.getMetaData().getURL();
			String product = c.getMetaData().getDatabaseProductName();

			return ResponseEntity.ok(Map.of(
					"timestamp", Instant.now().toString(),
					"ok", ok,
					"database", product,
					"url", url
			));
		}
	}
}

