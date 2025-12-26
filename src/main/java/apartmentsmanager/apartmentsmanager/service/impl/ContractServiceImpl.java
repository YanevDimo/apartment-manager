package apartmentsmanager.apartmentsmanager.service.impl;

import apartmentsmanager.apartmentsmanager.entity.Apartment;
import apartmentsmanager.apartmentsmanager.service.ContractService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class ContractServiceImpl implements ContractService {
    
    @Override
    public byte[] generateContract(Apartment apartment) {
        StringBuilder contract = new StringBuilder();
        
        contract.append("ДОГОВОР ЗА ПРОДАЖБА НА АПАРТАМЕНТ\n");
        contract.append("=====================================\n\n");
        
        contract.append("Днес, ").append(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append(" г.,\n\n");
        
        if (apartment.getClient() != null) {
            contract.append("КУПУВАЧ:\n");
            contract.append("Име: ").append(apartment.getClient().getName()).append("\n");
            if (apartment.getClient().getPhone() != null && !apartment.getClient().getPhone().isEmpty()) {
                contract.append("Телефон: ").append(apartment.getClient().getPhone()).append("\n");
            }
            if (apartment.getClient().getEmail() != null && !apartment.getClient().getEmail().isEmpty()) {
                contract.append("Email: ").append(apartment.getClient().getEmail()).append("\n");
            }
            if (apartment.getClient().getEgn() != null && !apartment.getClient().getEgn().isEmpty()) {
                contract.append("ЕГН/ЕИК: ").append(apartment.getClient().getEgn()).append("\n");
            }
            if (apartment.getClient().getAddress() != null && !apartment.getClient().getAddress().isEmpty()) {
                contract.append("Адрес: ").append(apartment.getClient().getAddress()).append("\n");
            }
            contract.append("\n");
        }
        
        contract.append("ПРЕДМЕТ НА ДОГОВОРА:\n");
        contract.append("Сграда: ").append(apartment.getBuildingName()).append("\n");
        contract.append("Апартамент №: ").append(apartment.getApartmentNumber()).append("\n");
        contract.append("Площ: ").append(apartment.getArea()).append(" кв.м\n");
        contract.append("Цена на кв.м: ").append(apartment.getPricePerM2()).append(" €\n");
        contract.append("Обща цена: ").append(apartment.getTotalPrice()).append(" €\n");
        
        if (apartment.getStage() != null && !apartment.getStage().isEmpty()) {
            contract.append("Етап: ").append(apartment.getStage()).append("\n");
        }
        
        if (apartment.getContractDate() != null) {
            contract.append("Дата на договор: ").append(apartment.getContractDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))).append("\n");
        }
        
        contract.append("\n");
        
        contract.append("ПЛАТЕЖНА ИНФОРМАЦИЯ:\n");
        contract.append("Общо платено: ").append(apartment.getTotalPaid()).append(" €\n");
        contract.append("Остатък за плащане: ").append(apartment.getRemainingPayment()).append(" €\n");
        
        if (apartment.getNotes() != null && !apartment.getNotes().isEmpty()) {
            contract.append("\n");
            contract.append("БЕЛЕЖКИ:\n");
            contract.append(apartment.getNotes()).append("\n");
        }
        
        contract.append("\n");
        contract.append("=====================================\n");
        contract.append("Генерирано на: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n");
        
        return contract.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public String getContractFilename(Apartment apartment) {
        String buildingName = apartment.getBuildingName().replaceAll("[^a-zA-Z0-9]", "_");
        String apartmentNumber = apartment.getApartmentNumber().replaceAll("[^a-zA-Z0-9]", "_");
        return String.format("Договор_%s_%s_%s.txt", 
                buildingName, 
                apartmentNumber,
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
}





