package sdProject.dao;

import sdProject.models.Disciplina;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {
    private Connection conn;
    public DisciplinaDAO(Connection conn) { this.conn = conn; }

    public void salvar(Disciplina disciplina) throws SQLException {
        String sql = "INSERT INTO disciplina (nome) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, disciplina.getNome());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    disciplina.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Disciplina findById(int id) throws SQLException {
        String sql = "SELECT * FROM disciplina WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Disciplina disciplina = new Disciplina();
                    disciplina.setId(rs.getInt("id"));
                    disciplina.setNome(rs.getString("nome"));
                    return disciplina;
                }
            }
        }
        return null;
    }

    public List<Disciplina> listarTodas() throws SQLException {
        List<Disciplina> disciplinas = new ArrayList<>();
        String sql = "SELECT * FROM disciplina ORDER BY nome";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Disciplina disciplina = new Disciplina();
                disciplina.setId(rs.getInt("id"));
                disciplina.setNome(rs.getString("nome"));
                disciplinas.add(disciplina);
            }
        }
        return disciplinas;
    }

    public boolean atualizar(Disciplina disciplina) throws SQLException {
        String sql = "UPDATE disciplina SET nome = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, disciplina.getNome());
            stmt.setInt(2, disciplina.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM disciplina WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}