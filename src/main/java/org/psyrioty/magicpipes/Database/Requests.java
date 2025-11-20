package org.psyrioty.magicpipes.Database;

import org.bukkit.Bukkit;
import org.psyrioty.magicpipes.magicpipes;
import org.psyrioty.magicpipes.Objects.Pipe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Requests {
    static String sqlLogin = magicpipes.getPlugin().getConfig().getString("sql.login");
    static String sqlPass = magicpipes.getPlugin().getConfig().getString("sql.password");
    static String jdbc = "jdbc:mysql://"
        + magicpipes.getPlugin().getConfig().getString("sql.ip") + ":"
        + magicpipes.getPlugin().getConfig().getString("sql.port") + "/"
        + magicpipes.getPlugin().getConfig().getString("sql.db_name") + "?user="
        + sqlLogin + "&password="
        + sqlPass;

    private static Connection connection;


    public static void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            connection = DriverManager.getConnection(jdbc, sqlLogin, sqlPass);
            Bukkit.getLogger().info("✅ Database connected!");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("❌ Failed to connect to database: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("❌ Connection check failed: " + e.getMessage());
        }
        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("🔌 Database disconnected!");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("❌ Failed to disconnect: " + e.getMessage());
        }
    }

    public static void createPipe(Pipe pipe) {
        String sql = "INSERT INTO magicpipe_pipe (x, y, z, world, type) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);


            stmt.setInt(1, pipe.getX());
            stmt.setInt(2, pipe.getY());
            stmt.setInt(3, pipe.getZ());
            stmt.setString(4, pipe.getWorld().getName());
            stmt.setInt(5, pipe.getType());

            stmt.executeUpdate();

        } catch (SQLException e) {
            Bukkit.getLogger().info("MagicPipe createPipe() error: " + e.getMessage());
        }
    }


    public static List<Pipe> getAllPipes() {
        List<Pipe> pipes = new ArrayList<>();
        String sql = "SELECT * FROM magicpipe_pipe";

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Pipe pipe = new Pipe(
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        Bukkit.getWorld(rs.getString("world")),
                        (byte) rs.getInt("type")
                );
                pipes.add(pipe);
            }
            Bukkit.getLogger().info("✅Все трубы загружены.");
        } catch (SQLException e) {
            Bukkit.getLogger().info("MagicPipe getAllPipes() error: " + e.getMessage());
        } catch (Exception e) {
            Bukkit.getLogger().info("MagicPipe getAllPipes() error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return pipes;
    }


    public static void removePipe(Pipe pipe) {
        String sqlSelect = "SELECT id FROM magicpipe_pipe WHERE x = ? AND y = ? AND z = ? AND world = ?";
        String sqlDeletePipe = "DELETE FROM magicpipe_pipe WHERE x = ? AND y = ? AND z = ? AND world = ?";
        String sqlDeleteItems = "DELETE FROM magicpipe_items WHERE pipe_id = ?";

        try {
            Connection conn = getConnection();

            int id = 0;

            // 1. Получаем ID пайпа
            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, pipe.getX());
                stmtSelect.setInt(2, pipe.getY());
                stmtSelect.setInt(3, pipe.getZ());
                stmtSelect.setString(4, pipe.getWorld().getName());

                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt("id");
                    }
                }
            }

            // Если пайп не найден, просто выходим
            if (id == 0) {
                Bukkit.getLogger().warning("removePipe(): pipe not found!");
                return;
            }

            // 2. Удаляем сам pipe
            try (PreparedStatement stmtDeletePipe = conn.prepareStatement(sqlDeletePipe)) {
                stmtDeletePipe.setInt(1, pipe.getX());
                stmtDeletePipe.setInt(2, pipe.getY());
                stmtDeletePipe.setInt(3, pipe.getZ());
                stmtDeletePipe.setString(4, pipe.getWorld().getName());
                stmtDeletePipe.executeUpdate();
            }

            // 3. Удаляем связанные items
            try (PreparedStatement stmtDeleteItems = conn.prepareStatement(sqlDeleteItems)) {
                stmtDeleteItems.setInt(1, id);
                stmtDeleteItems.executeUpdate();
            }

        } catch (SQLException e) {
            Bukkit.getLogger().info("MagicPipe removePipe() error: " + e.getMessage());
        }
    }


    public static void itemsAdd(Pipe pipe, List<String> base64List, List<Integer> slot) {
        String sqlPipe = "SELECT id FROM magicpipe_pipe WHERE x = ? AND y = ? AND z = ? AND world = ?";
        String sqlDelete = "DELETE FROM magicpipe_items WHERE pipe_id = ?";
        String sqlInsert = "INSERT INTO magicpipe_items (base64, pipe_id, slot) VALUES (?, ?, ?)";
        try {
            Connection conn = getConnection();

            // 1. Получаем id
            int id = 0;
            try (PreparedStatement stmtPipe = conn.prepareStatement(sqlPipe)) {
                stmtPipe.setInt(1, pipe.getX());
                stmtPipe.setInt(2, pipe.getY());
                stmtPipe.setInt(3, pipe.getZ());
                stmtPipe.setString(4, pipe.getWorld().getName());

                try (ResultSet rs = stmtPipe.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt("id");
                    }
                }
            }

            if (id == 0) {
                Bukkit.getLogger().warning("itemsAdd(): pipe not found!");
                return;
            }

            // 2. Удаляем старые items
            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                stmtDelete.setInt(1, id);
                stmtDelete.executeUpdate();
            }

            // 3. Вставляем новые items (batch для скорости)
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                for (int i = 0; i < base64List.size(); i++) {
                    stmtInsert.setString(1, base64List.get(i));
                    stmtInsert.setInt(2, id);
                    stmtInsert.setInt(3, slot.get(i));
                    stmtInsert.addBatch();
                }
                stmtInsert.executeBatch();
            }

        } catch (SQLException e) {
            Bukkit.getLogger().info("MagicPipe itemsAdd() error: " + e.getMessage());
        }
    }


    public static List<String> getAllItems(Pipe pipe) {
        List<String> base64List = new ArrayList<>();

        String sqlPipe = "SELECT id FROM magicpipe_pipe WHERE x = ? AND y = ? AND z = ? AND world = ?";
        String sqlItems = "SELECT slot, base64 FROM magicpipe_items WHERE pipe_id = ?";

        try {
            Connection conn = getConnection();
            PreparedStatement stmtPipe = conn.prepareStatement(sqlPipe);
            stmtPipe.setInt(1, pipe.getX());
            stmtPipe.setInt(2, pipe.getY());
            stmtPipe.setInt(3, pipe.getZ());
            stmtPipe.setString(4, pipe.getWorld().getName());

            try (ResultSet rsPipe = stmtPipe.executeQuery()) {
                if (rsPipe.next()) {
                    int id = rsPipe.getInt("id");

                    try (PreparedStatement stmtItems = conn.prepareStatement(sqlItems)) {
                        stmtItems.setInt(1, id);
                        try (ResultSet rsItems = stmtItems.executeQuery()) {
                            while (rsItems.next()) {
                                String base64 = rsItems.getInt("slot") + " " + rsItems.getString("base64");
                                base64List.add(base64);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().info("MagicPipe getAllItems() error: " + e.getMessage());
        }

        return base64List;
    }


    public static void removeItem(Pipe pipe) {
        String sqlSelect = "SELECT id FROM magicpipe_pipe WHERE x = ? AND y = ? AND z = ? AND world = ?";
        String sqlDelete = "DELETE FROM magicpipe_items WHERE pipe_id = ?";

        try {
            Connection conn = getConnection();

            int id = 0;

            // Получаем id пайпа
            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, pipe.getX());
                stmtSelect.setInt(2, pipe.getY());
                stmtSelect.setInt(3, pipe.getZ());
                stmtSelect.setString(4, pipe.getWorld().getName());

                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt("id");
                    }
                }
            }

            if (id == 0) {
                Bukkit.getLogger().warning("removeItem(): pipe not found!");
                return;
            }

            // Удаляем items
            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                stmtDelete.setInt(1, id);
                stmtDelete.executeUpdate();
            }

        } catch (SQLException e) {
            Bukkit.getLogger().info("MagicPipe removeItem() error: " + e.getMessage());
        }
    }

}
