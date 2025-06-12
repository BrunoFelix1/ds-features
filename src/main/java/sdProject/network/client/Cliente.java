package sdProject.network.client;

import sdProject.config.AppConfig;
import sdProject.network.util.Connection;
import sdProject.network.util.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Cliente {
    
    private static final String MATRICULA_SERVICE = "matricula";
    private static final String NOTA_SERVICE = "nota";
    private static final String HISTORICO_SERVICE = "historico";
    
    private final String gatewayHost;
    private final int gatewayPort;      // Cache de localização dos serviços (serviço -> endereço:porta)
    private final Map<String, ServiceLocation> serviceCache = new ConcurrentHashMap<>();
    
    private static final int UDP_TIMEOUT_MS = AppConfig.getUdpTimeoutMs();

    public Cliente(String gatewayHost, int gatewayPort) {
        this.gatewayHost = gatewayHost; // ISSO AQUI É O UNICO IP DA APLICAÇÃO BASICAMENTE, é setado nas mains dos serviços
        this.gatewayPort = gatewayPort;
    }    @SuppressWarnings("unchecked") // é so pra nao aparecer o erro do cast de objeto para map ali no retorno


    //MÉTODOS DE COMUNICAÇÃO INTERNA

    private Map<String, Object> sendToGatewayUDP(Map<String, Object> requestPayload) throws IOException, ClassNotFoundException {
        try (Connection connection = new Connection()) {
            connection.setSoTimeout(UDP_TIMEOUT_MS);
            InetAddress gwAddress = InetAddress.getByName(gatewayHost);
            
            connection.sendUDP(requestPayload, gwAddress, gatewayPort);
            DatagramPacket receivePacket = connection.receiveUDP();
            
            byte[] actualData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), actualData, 0, receivePacket.getLength());
            
            return (Map<String, Object>) SerializationUtils.deserialize(actualData);
        }
    }

    private ServiceLocation discoverService(String serviceName) throws IOException, ClassNotFoundException {
        ServiceLocation cachedLocation = serviceCache.get(serviceName);
        if (cachedLocation != null) {
            // disponibilidae dele aqui
            try (Connection testConnection = new Connection(cachedLocation.getHost(), cachedLocation.getPort())) {
                return cachedLocation;  // Serviço ainda está ativo
            } catch (ConnectException e) {
                // Serviço não está disponível, entao a gnt remove ele
                serviceCache.remove(serviceName);
                System.out.println("Serviço " + serviceName + " não está mais disponível.");
            }
        }
        
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "discover");
        request.put("serviceName", serviceName);

        try {
            Map<String, Object> response = sendToGatewayUDP(request);
            if ("success".equals(response.get("status"))){
                String serviceAddress = (String) response.get("serviceAddress"); // Aqui recebemos o enderço TCP do Worker
                ServiceLocation location = new ServiceLocation(serviceAddress);
                serviceCache.put(serviceName, location);
                System.out.println("Serviço " + serviceName + " descoberto no endereço " + serviceAddress);
                return location;
            } else {
                System.err.println("Falha ao descobrir serviço " + serviceName + " " + response.get("message"));
                return findAlternativeService(serviceName);
            }
        } catch (SocketTimeoutException e){
            System.err.println("Timeout ao descobrir serviço " + serviceName + " do Gateway UDP.");
            return findAlternativeService(serviceName);
        }
    }

    @SuppressWarnings("unchecked")
    private ServiceLocation findAlternativeService(String serviceType) throws IOException, ClassNotFoundException {
        System.out.println("Procurando serviços alternativos para " + serviceType);
        
        Map<String, Object> request = new HashMap<>();
        request.put("operation", "getServices");
        request.put("serviceType", serviceType);

        try {
            Map<String, Object> response = sendToGatewayUDP(request);
            if ("success".equals(response.get("status"))){
                Map<String, String> services = (Map<String, String>) response.get("services");
                if (services != null && !services.isEmpty()){
                    // Aqui pega o primeiro serviço alternativo encontrado
                   Map.Entry<String, String> entry = services.entrySet().iterator().next();
                   String altServiceName = entry.getKey();
                   String serviceAddress = entry.getValue(); // Endereço TCP do worker

                    ServiceLocation location = new ServiceLocation(serviceAddress);
                    serviceCache.put(serviceType, location);
                    System.out.println("Usando serviço alternativo " + altServiceName + " em " + serviceAddress + " para tipo " + serviceType);
                    return location;
                }
            }
            System.err.println("Gateway não retornou serviços alternativos para " + serviceType + response.get("message"));
        } catch (SocketTimeoutException e){
            System.err.println("Timeout ao buscar serviços alternativos no Gateway para " + serviceType);
        }
        throw new IOException("Nenhum serviço do tipo " + serviceType + " disponível via Gateway.");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> callService(String serviceName, Map<String, Object> request) 
            throws IOException, ClassNotFoundException {
        
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                ServiceLocation location = discoverService(serviceName);
                  try (Connection connection = new Connection(location.getHost(), location.getPort())) {
                    connection.send(request);
                    return (Map<String, Object>) connection.receive();
                } catch (ConnectException e) {
                    serviceCache.remove(serviceName);
                    System.out.println("Falha na conexão com " + serviceName + ". Tentando novamente...");
                    retryCount++;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao chamar serviço " + serviceName + ": " + e.getMessage());
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        throw new IOException("Falha após " + maxRetries + " tentativas de chamar o serviço " + serviceName);
    }

    //MENU'S DOS SERVIÇOS
    // MENU MATRÍCULA 
    public void menuMatricula(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Matricular aluno \n" +
            "2.Verificar matrícula \n" +
            "3.Listar matrículas por aluno \n" +
            "4.Listar matrículas por disciplina \n" +
            "5.Cancelar matrícula \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            if (escolhaDoSubservico == 0) {
                System.out.println("Voltando...");
                break;
            }

            System.out.println("Digite o ID do aluno: ");
            int alunoId = scanner.nextInt();
            System.out.println("Digite o ID da disciplina: ");
            int disciplinaId = scanner.nextInt();
    
            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Matriculando aluno " + alunoId + " na disciplina " + disciplinaId);
                    
                    //CHAMA O CLIENTE 
                    Map<String, Object> resultadoMatricula = this.matricularAluno(alunoId, disciplinaId);
                    
                    if ("success".equals(resultadoMatricula.get("status"))) {
                        System.out.println("Matrícula realizada com sucesso!");
                        System.out.println("Mensagem: " + resultadoMatricula.get("message"));
                    } else {
                        System.out.println("Erro na matrícula: " + resultadoMatricula.get("message"));
                    }
                    break;
                    
                case 2:
                    System.out.println("Verificando matrícula do aluno " + alunoId + " na disciplina " + disciplinaId);
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoVerificar = this.verificarMatricula(alunoId, disciplinaId);
                    
                    if ("success".equals(resultadoVerificar.get("status"))) {
                        boolean matriculado = (Boolean) resultadoVerificar.get("matriculado");
                        System.out.println("Aluno matriculado? " + matriculado);
                    } else {
                        System.out.println("Erro ao verificar: " + resultadoVerificar.get("message"));
                    }
                    break;
                    
                case 3:
                    System.out.println("Listando matrículas do aluno " + alunoId);
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoListarAluno = this.listarMatriculasPorAluno(alunoId);
                    
                    if ("success".equals(resultadoListarAluno.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> matriculas = (List<Map<String, Object>>) resultadoListarAluno.get("matriculas");
                        
                        if (matriculas.isEmpty()) {
                            System.out.println("Nenhuma matrícula encontrada para o aluno ID " + alunoId);
                        } else {
                            for (Map<String, Object> matricula : matriculas) {
                                System.out.println("Disciplina ID: " + matricula.get("disciplinaId") + " | Nota: " + matricula.get("nota"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao listar: " + resultadoListarAluno.get("message"));
                    }
                    break;
                    
                case 4:
                    System.out.println("Listando matrículas da disciplina " + disciplinaId);
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoListarDisciplina = this.listarMatriculasPorDisciplina(disciplinaId);
                    
                    if ("success".equals(resultadoListarDisciplina.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> matriculas = (List<Map<String, Object>>) resultadoListarDisciplina.get("matriculas");
                        
                        if (matriculas.isEmpty()) {
                            System.out.println("Nenhuma matrícula encontrada para a disciplina ID " + disciplinaId);
                        } else {
                            System.out.println("Matrículas da disciplina ID " + disciplinaId + ":");
                            for (Map<String, Object> matricula : matriculas) {
                                System.out.println("Aluno ID: " + matricula.get("alunoId") + " | Nota: " + matricula.get("nota"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao listar: " + resultadoListarDisciplina.get("message"));
                    }
                    break;
                    
                case 5:
                    System.out.println("Cancelando matrícula do aluno " + alunoId + " na disciplina " + disciplinaId);
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoCancelar = this.cancelarMatricula(alunoId, disciplinaId);
                    
                    if ("success".equals(resultadoCancelar.get("status"))) {
                        System.out.println("Matrícula cancelada com sucesso!");
                        System.out.println("Mensagem: " + resultadoCancelar.get("message"));
                    } else {
                        System.out.println("Erro ao cancelar matrícula: " + resultadoCancelar.get("message"));
                    }
                    break;
                    
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        } while (escolhaDoSubservico != 0);
    }

    //MENU NOTA 
    public void menuNota(Integer escolhaDoSubservico, Scanner scanner) {
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Registrar nota \n" +
            "2.Consultar nota \n" +
            "3.Calcular média aluno \n" +
            "4.Calcular média disciplina \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            if (escolhaDoSubservico == 0) {
                System.out.println("Voltando...");
                break;
            }

            System.out.println("Digite o ID do aluno: ");
            int alunoId = scanner.nextInt();
            System.out.println("Digite o ID da disciplina: ");
            int disciplinaId = scanner.nextInt(); 

            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Digite a nota: ");
                    double nota = scanner.nextDouble();
                    
                    //CHAMA O CLIENTE DISTRIBUÍDO
                    Map<String, Object> resultadoNota = this.registrarNota(alunoId, disciplinaId, nota);
                    
                    if ("success".equals(resultadoNota.get("status"))) {
                        System.out.println("Nota registrada com sucesso!");
                        System.out.println("Mensagem: " + resultadoNota.get("message"));
                    } else {
                        System.out.println("Erro ao registrar nota: " + resultadoNota.get("message"));
                    }
                    break;
                    
                case 2:
                    System.out.println("Consultando nota...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoConsulta = this.consultarNota(alunoId, disciplinaId);
                    
                    if ("success".equals(resultadoConsulta.get("status"))) {
                        Double notaConsultada = (Double) resultadoConsulta.get("nota");
                        if (notaConsultada != null) {
                            System.out.println("Nota do aluno " + alunoId + " na disciplina " + disciplinaId + ": " + notaConsultada);
                        } else {
                            System.out.println("Nota não encontrada");
                        }
                    } else {
                        System.out.println("Erro ao consultar: " + resultadoConsulta.get("message"));
                    }
                    break;
                    
                case 3:
                    System.out.println("Calculando média do aluno...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoMediaAluno = this.calcularMediaAluno(alunoId);
                    
                    if ("success".equals(resultadoMediaAluno.get("status"))) {
                        Double media = (Double) resultadoMediaAluno.get("media");
                        if (media != null) {
                            System.out.println("Média do aluno ID " + alunoId + ": " + media);
                        } else {
                            System.out.println("Não foi possível calcular a média");
                        }
                    } else {
                        System.out.println("Erro ao calcular média: " + resultadoMediaAluno.get("message"));
                    }
                    break;
                    
                case 4:
                    System.out.println("Calculando média da disciplina...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoMediaDisciplina = this.calcularMediaDisciplina(disciplinaId);
                    
                    if ("success".equals(resultadoMediaDisciplina.get("status"))) {
                        Double media = (Double) resultadoMediaDisciplina.get("media");
                        if (media != null) {
                            System.out.println("Média da disciplina ID " + disciplinaId + ": " + media);
                        } else {
                            System.out.println("Não foi possível calcular a média");
                        }
                    } else {
                        System.out.println("Erro ao calcular média: " + resultadoMediaDisciplina.get("message"));
                    }
                    break;
                    
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }

        } while (escolhaDoSubservico != 0);
    }

    //MENU HISTÓRICO 
    public void menuHistorico(Integer escolhaDoSubservico, Scanner scanner){
        escolhaDoSubservico = -1;

        do{
            System.out.println("Qual subserviço você quer acessar?");
            System.out.println("1.Gerar histórico completo \n" +
            "2.Listar disciplinas aprovadas \n" +
            "3.Listar disciplinas reprovadas \n" +
            "4.Listar disciplinas em curso \n" +
            "5.Calcular coeficiente de rendimento \n" +
            "0.Voltar");

            escolhaDoSubservico = scanner.nextInt();

            if (escolhaDoSubservico == 0) {
                System.out.println("Voltando...");
                break;
            }

            System.out.println("Digite o ID do aluno: ");
            int alunoId = scanner.nextInt(); 

            switch (escolhaDoSubservico) {
                case 1:
                    System.out.println("Gerando histórico completo...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoHistorico = this.gerarHistoricoCompleto(alunoId);
                    
                    if ("success".equals(resultadoHistorico.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> historico = (List<Map<String, Object>>) resultadoHistorico.get("historico");
                        
                        if (historico.isEmpty()) {
                            System.out.println("Nenhum registro encontrado para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Histórico do aluno ID " + alunoId + ":");
                            for (Map<String, Object> entrada : historico) {
                                System.out.println("Disciplina: " + entrada.get("disciplinaNome") + " | Nota: " + entrada.get("nota"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao gerar histórico: " + resultadoHistorico.get("message"));
                    }
                    break;
                    
                case 2:
                    System.out.println("Listando disciplinas aprovadas...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoAprovadas = this.listarDisciplinasAprovadas(alunoId);
                    processarResultadoDisciplinas(resultadoAprovadas, "aprovadas", alunoId);
                    break;
                    
                case 3:
                    System.out.println("Listando disciplinas reprovadas...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoReprovadas = this.listarDisciplinasReprovadas(alunoId);
                    processarResultadoDisciplinas(resultadoReprovadas, "reprovadas", alunoId);
                    break;
                    
                case 4:
                    System.out.println("Listando disciplinas em curso...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoEmCurso = this.listarDisciplinasEmCurso(alunoId);
                    
                    if ("success".equals(resultadoEmCurso.get("status"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) resultadoEmCurso.get("disciplinas");
                        
                        if (disciplinas.isEmpty()) {
                            System.out.println("Nenhuma disciplina em curso encontrada para o aluno ID " + alunoId);
                        } else {
                            System.out.println("Disciplinas em curso do aluno ID " + alunoId + ":");
                            for (Map<String, Object> disciplina : disciplinas) {
                                System.out.println("Disciplina: " + disciplina.get("nome"));
                            }
                        }
                    } else {
                        System.out.println("Erro ao listar disciplinas em curso: " + resultadoEmCurso.get("message"));
                    }
                    break;
                    
                case 5:
                    System.out.println("Calculando coeficiente de rendimento...");
                    
                    //CHAMA O CLIENTE
                    Map<String, Object> resultadoCoeficiente = this.calcularCoeficienteRendimento(alunoId);
                    
                    if ("success".equals(resultadoCoeficiente.get("status"))) {
                        Double coeficiente = (Double) resultadoCoeficiente.get("coeficiente");
                        if (coeficiente != null) {
                            System.out.println("Coeficiente de rendimento do aluno ID " + alunoId + ": " + coeficiente);
                        } else {
                            System.out.println("Não foi possível calcular o coeficiente de rendimento");
                        }
                    } else {
                        System.out.println("Erro ao calcular coeficiente: " + resultadoCoeficiente.get("message"));
                    }
                    break;
                    
                default:
                    System.out.println("Opção inválida, tente novamente.");
                    break;
            }

        } while (escolhaDoSubservico != 0);
    }

    // ESSAS FUNCOES AQUI SÃO EXEMPLOS DE COMO CHAMAR OS SERVIÇOS, A GENTE PODE MUDAR DEPOIS
    // MAS O IMPORTANTE É QUE ELAS CHAMAM O GATEWAY E RETORNAM O RESULTADO
    // E MOSTRAM AS CARACTERISISTICAS DE SD

        //Em relação a matrícula
    public Map<String, Object> matricularAluno(int alunoId, int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "matricular");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId); 
        
        try {
            return callService(MATRICULA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao matricular aluno: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> verificarMatricula(int alunoId, int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "verificarMatricula");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId);
        
        try {
            return callService(MATRICULA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao verificar matrícula: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> listarMatriculasPorAluno(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "buscarPorAluno");
        request.put("alunoId", alunoId);
        
        try {
            return callService(MATRICULA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao listar matrículas por aluno: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> listarMatriculasPorDisciplina(int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "buscarPorDisciplina");
        request.put("disciplinaId", disciplinaId);
        
        try {
            return callService(MATRICULA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao listar matrículas por disciplina: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> cancelarMatricula(int alunoId, int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "cancelar");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId);
        
        try {
            return callService(MATRICULA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao cancelar matrícula: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

       //Em relação a nota
    public Map<String, Object> registrarNota(int alunoId, int disciplinaId, Double nota) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "registrarNota");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId);
        request.put("nota", nota);
        
        try {
            return callService(NOTA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao registrar nota: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> consultarNota(int alunoId, int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "consultarNota");
        request.put("alunoId", alunoId);
        request.put("disciplinaId", disciplinaId);
        
        try {
            return callService(NOTA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao consultar nota: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> calcularMediaAluno(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "calcularMediaAluno");
        request.put("alunoId", alunoId);
        
        try {
            return callService(NOTA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao calcular média do aluno: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> calcularMediaDisciplina(int disciplinaId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "calcularMediaDisciplina");
        request.put("disciplinaId", disciplinaId);
        
        try {
            return callService(NOTA_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao calcular média da disciplina: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

        //Em relação a histórico
    public Map<String, Object> gerarHistoricoCompleto(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "historicoCompleto");
        request.put("alunoId", alunoId);
        
        try {
            return callService(HISTORICO_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao gerar histórico: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }
    
    public Map<String, Object> listarDisciplinasAprovadas(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "disciplinasAprovadas");
        request.put("alunoId", alunoId);
        
        try {
            return callService(HISTORICO_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao listar disciplinas aprovadas: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> listarDisciplinasReprovadas(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "disciplinasReprovadas");
        request.put("alunoId", alunoId);
        
        try {
            return callService(HISTORICO_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao listar disciplinas reprovadas: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> listarDisciplinasEmCurso(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "disciplinasEmCurso");
        request.put("alunoId", alunoId);
        
        try {
            return callService(HISTORICO_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao listar disciplinas em curso: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    public Map<String, Object> calcularCoeficienteRendimento(int alunoId) {
        Map<String, Object> request = new HashMap<>();
        request.put("action", "calcularCR");
        request.put("alunoId", alunoId);
        
        try {
            return callService(HISTORICO_SERVICE, request);
        } catch (Exception e) {
            System.err.println("Erro ao calcular coeficiente de rendimento: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Erro de comunicação: " + e.getMessage());
            return errorResponse;
        }
    }

    // MÉTODO AUXILIAR para processar resultados de disciplinas
    private static void processarResultadoDisciplinas(Map<String, Object> resultado, String tipo, int alunoId) {
        if ("success".equals(resultado.get("status"))) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> disciplinas = (List<Map<String, Object>>) resultado.get("disciplinas");
            
            if (disciplinas.isEmpty()) {
                System.out.println("Nenhuma disciplina " + tipo + " encontrada para o aluno ID " + alunoId);
            } else {
                System.out.println("Disciplinas " + tipo + " do aluno ID " + alunoId + ":");
                for (Map<String, Object> entrada : disciplinas) {
                    System.out.println("Disciplina: " + entrada.get("disciplinaNome") + " | Nota: " + entrada.get("nota"));
                }
            }
        } else {
            System.out.println("Erro ao listar disciplinas " + tipo + ": " + resultado.get("message"));
        }
    }

    public static void main (String[] args){
        // essa é a unica definicao de ip da aplicação basicamente, o resto
        // é tudo dinâmico, a partir do gateway discovery
        String gatewayHost = AppConfig.getGatewayHost();
        int gatewayPort = AppConfig.getGatewayPort();
        
        // Verificar se host e porta foram passados como argumentos (sobrescreve as configurações)
        if (args.length >= 2) {
            gatewayHost = args[0];
            gatewayPort = Integer.parseInt(args[1]);
        }
        
        Cliente cliente = new Cliente(gatewayHost, gatewayPort); 

        //Menu do nosso sistema
        Scanner scanner = new Scanner(System.in);
        Integer escolhaDoServico = -1;

        do{
            System.out.println("============================================================");
            System.out.println("SEJA BEM VINDO AO SISTEMA DE GERÊNCIAMENTO ACADÊMICO!");
            System.out.println("Qual serviço você quer acessar?(Digite um número de 1-4)");
            System.out.println("1.Matrícula \n2.Nota \n3.Histórico \n4.Sair");
            System.out.println("============================================================");

            escolhaDoServico = scanner.nextInt();
            
            switch (escolhaDoServico) {
                case 1:
                    cliente.menuMatricula(escolhaDoServico, scanner);

                    break;
                case 2:
                    cliente.menuNota(escolhaDoServico, scanner);
                    break; 
                case 3:
                    cliente.menuHistorico(escolhaDoServico, scanner);
                    break;
                case 4:
                    scanner.close();
                    System.out.println("Encerrando sistema...");
                    System.exit(0);
                    break;
            
                default:
                    System.out.println("Digite um número válido!");
                    break;
            }

        } while(escolhaDoServico != 0);

        scanner.close();

    }


}
