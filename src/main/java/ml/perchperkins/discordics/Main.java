package ml.perchperkins.discordics;

import io.github.cdimascio.dotenv.Dotenv;
import ml.perchperkins.discordics.controllers.CalendarController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import spark.Spark;

public class Main {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		final String token = dotenv.get("TOKEN");
		JDA jda = JDABuilder.createDefault(token).build();

		Spark.get("/calendar", new CalendarController(jda));
	}
}