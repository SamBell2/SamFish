package chess.syzygy;

//import chess.Board;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Collectors;

import chess.Bot;

public class DTZProbe {
    public static int probe (String FEN, boolean white, int fullMoves, int halfMoves) {
        Process process;
        FEN += white ? (" w - - " + Integer.toString(fullMoves) + " " + Integer.toString(halfMoves)) : " b - - " + Integer.toString(fullMoves) + " " + Integer.toString(halfMoves);
        try {
            process = new ProcessBuilder("/home/arco/Documents/Java/SamFish/fathom/src/apps/fathom.linux", "--path=/home/arco/syzygy", /* "\"" + */FEN/*  + "\"" */)
                .redirectErrorStream(true)
                .start();
            Bot.logger.info(new ProcessBuilder("/home/arco/Documents/Java/SamFish/fathom/src/apps/fathom.linux", "--path=/home/arco/syzygy", "\"" +FEN + "\"")
                .redirectErrorStream(true).command().stream().collect(Collectors.joining(" ")));
        } catch (IOException e) {
            Bot.logger.fatal(e.getMessage());
            return -1;
        }

        Scanner sc = new Scanner(process.getInputStream());
        String line;
        int result = 0;
        Bot.logger.info("Checking output");
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            // Bot.logger.info(line);
            if (line.startsWith("[DTZ")) {
                String[] parts = line.split("\"");
                // for (String part : parts) Bot.logger.info(part);
                // Bot.logger.info(parts[1]);
                result = Integer.parseInt(parts[1]);
                break;
            }
        }
        sc.close();
        Bot.logger.info(result);
        return result;
    }
}