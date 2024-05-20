package com.example.mspersonas.service;

import com.example.mspersonas.dto.ApiException;
import com.example.mspersonas.dto.CreatePersonDto;
import com.example.mspersonas.dto.CreatePersonResponseDto;
import com.example.mspersonas.dto.GetPersonDto;
import com.example.mspersonas.enums.PersonStatus;
import com.example.mspersonas.model.Person;
import com.example.mspersonas.repository.PersonRepository;
import org.springframework.stereotype.Service;

import static com.example.mspersonas.enums.ErrorCode.EV001;
import static com.example.mspersonas.enums.ErrorCode.EV003;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    PersonService(final PersonRepository personRepository){
        this.personRepository = personRepository;
    }
    public CreatePersonResponseDto addPerson(final CreatePersonDto createPersonDto){
        var existingRecord = personRepository.findByDni(createPersonDto.dni());
        var  errorMsg = existingRecord.map(Person::getStatus).map(PersonStatus::getCreateError)
                .orElse("");
        if(!errorMsg.isBlank()) throw new ApiException(EV001, errorMsg);

        var newPerson = existingRecord.map(p -> {p.setStatus(PersonStatus.ACTIVO); return p;})
                .orElseGet(() -> Person.builder()
                        .dni(createPersonDto.dni())
                        .type(createPersonDto.type())
                        .name(createPersonDto.name())
                        .lastName(createPersonDto.lastName())
                        .status(PersonStatus.ACTIVO)
                        .build()
        );

        var result = personRepository.save(newPerson);
        return new CreatePersonResponseDto(result.getId().toString());
    }

    public GetPersonDto getPerson(String id) {
        return this.personRepository.findById(id).
                map(person -> new GetPersonDto(
                        person.getId(),
                        person.getName(),
                        person.getLastName(),
                        person.getDni(),
                        person.getStatus(),
                        person. getType()))
                .orElseThrow(() -> new ApiException(EV003, notFoundErrorMsg(id)));
    }

    private String notFoundErrorMsg(String id) {
        return String.format("Person with id %s not found", id);
    }
}