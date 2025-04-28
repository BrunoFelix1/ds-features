package sdProject.dao;

import sdProject.models.Matricula;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatriculaDAO {
    private Connection conn;
    public MatriculaDAO(Connection conn) { this.conn = conn; }

    public void salvar(Matricula matricula) throws SQLException {
        String sql = "INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, matricula.getAlunoId());
            stmt.setInt(2, matricula.getDisciplinaId());
            if (matricula.getNota() != null) stmt.setDouble(3, matricula.getNota());
            else stmt.setNull(3, Types.NUMERIC);
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    matricula.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    public Matricula findById(int id) throws SQLException {
        String sql = "SELECT * FROM matricula WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Matricula matricula = new Matricula();
                    matricula.setId(rs.getInt("id"));
                    matricula.setAlunoId(rs.getInt("aluno_id"));
                    matricula.setDisciplinaId(rs.getInt("disciplina_id"));
                    if (rs.getObject("nota") != null) {
                        matricula.setNota(rs.getDouble("nota"));
                    }
                    return matricula;
                }
            }
        }
        return null;
    }
    
    public List<Matricula> findByAlunoId(int alunoId) throws SQLException {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT * FROM matricula WHERE aluno_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alunoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Matricula matricula = new Matricula();
                    matricula.setId(rs.getInt("id"));
                    matricula.setAlunoId(rs.getInt("aluno_id"));
                    matricula.setDisciplinaId(rs.getInt("disciplina_id"));
                    if (rs.getObject("nota") != null) {
                        matricula.setNota(rs.getDouble("nota"));
                    }
                    matriculas.add(matricula);
                }
            }
        }
        return matriculas;
    }
    
    public List<Matricula> findByDisciplinaId(int disciplinaId) throws SQLException {
        List<Matricula> matriculas = new ArrayList<>();
        String sql = "SELECT * FROM matricula WHERE disciplina_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, disciplinaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Matricula matricula = new Matricula();
                    matricula.setId(rs.getInt("id"));
                    matricula.setAlunoId(rs.getInt("aluno_id"));
                    matricula.setDisciplinaId(rs.getInt("disciplina_id"));
                    if (rs.getObject("nota") != null) {
                        matricula.setNota(rs.getDouble("nota"));
                    }
                    matriculas.add(matricula);
                }
            }
        }
        return matriculas;
    }
    
    public boolean atualizarNota(int alunoId, int disciplinaId, Double nota) throws SQLException {
        String sql = "UPDATE matricula SET nota = ? WHERE aluno_id = ? AND disciplina_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (nota != null) {
                stmt.setDouble(1, nota);
            } else {
                stmt.setNull(1, Types.NUMERIC);
            }
            stmt.setInt(2, alunoId);
            stmt.setInt(3, disciplinaId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM matricula WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
