package com.g8e.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.g8e.db.models.DBAccount;
import com.g8e.db.models.DBPlayer;
import com.g8e.gameserver.util.SkillUtils;
import com.google.gson.Gson;

public class CommonQueries {

    public static DBAccount getAccountByLoginToken(String socketId) throws SQLException {
        String query = "SELECT * FROM accounts WHERE login_token = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, socketId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new DBAccount(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("login_token"), rs.getString("registration_ip"),
                        rs.getString("registration_date"));
            }
            return null;
        }
    }

    public static DBPlayer getPlayerByAccountId(int accountId) throws SQLException {
        String query = "SELECT * FROM players WHERE account_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new DBPlayer(
                        rs.getInt("player_id"),
                        rs.getInt("account_id"),
                        rs.getInt("world_x"),
                        rs.getInt("world_y"),
                        rs.getInt("weapon"),
                        parseIntArray(rs.getString("inventory")),
                        parseIntArray(rs.getString("quest_progress")),
                        rs.getInt("attack_experience"),
                        rs.getInt("defence_experience"),
                        rs.getInt("strength_experience"),
                        rs.getInt("hitpoints_experience"));

            }
            return null;
        }
    }

    private static int[] parseIntArray(String str) {
        return (new Gson().fromJson(str, int[].class));

    }

    public static void savePlayerPositionByAccountId(int accountId, int x, int y) throws SQLException {
        String query = "UPDATE players SET world_x = ?, world_y = ? WHERE account_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
        }
    }

    public static void savePlayerXpByAccountId(int accountId, int skill, int xp) throws SQLException {
        String skillName = SkillUtils.getSkillNameByNumber(skill);
        String query = "UPDATE players SET " + skillName + "_experience = ? WHERE account_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, xp);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    public static void savePlayerInventoryByAccountId(int accountId, String inventory) throws SQLException {
        String query = "UPDATE players SET inventory = ? WHERE account_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, inventory);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

    public static void savePlayerWeaponByAccountId(int accountId, int weaponID) throws SQLException {
        String query = "UPDATE players SET weapon = ? WHERE account_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, weaponID);
            stmt.setInt(2, accountId);
            stmt.executeUpdate();
        }
    }

}
