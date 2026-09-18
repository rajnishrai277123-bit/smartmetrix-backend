package com.smartmetrix.backend.instrument;

import com.smartmetrix.backend.instrument.dto.UpdateInstrumentRequest;
import com.smartmetrix.backend.instrument.exception.DuplicateSerialNumberException;
import com.smartmetrix.backend.instrument.exception.InstrumentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public Instrument saveInstrument(Instrument instrument) {

        String serialNumber = instrument.getSerialNumber().trim();

        instrument.setSerialNumber(serialNumber);

        if (instrumentRepository.existsBySerialNumber(serialNumber)) {

            throw new DuplicateSerialNumberException(
                    "Serial number already exists");
        }

        return instrumentRepository.save(instrument);
    }

    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findAll();
    }

    public Instrument getInstrumentById(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new InstrumentNotFoundException("Instrument not found"));
    }

    public Instrument updateInstrument(Long id, UpdateInstrumentRequest request) {

        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() ->
                        new InstrumentNotFoundException("Instrument not found"));

        String serialNumber = request.getSerialNumber().trim();

        if (instrumentRepository.existsBySerialNumberAndIdNot(serialNumber, id)) {
            throw new DuplicateSerialNumberException(
                    "Serial number already exists");
        }

        instrument.setSerialNumber(serialNumber);
        instrument.setManufacturer(request.getManufacturer());
        instrument.setModel(request.getModel());
        instrument.setInstrumentClass(request.getInstrumentClass());
        instrument.setCapacity(request.getCapacity());
        instrument.setScaleInterval(request.getScaleInterval());
        instrument.setMinCapacity(request.getMinCapacity());
        instrument.setStatus(request.getStatus());

        return instrumentRepository.save(instrument);
    }


    public void deleteInstrument(Long id){
        instrumentRepository.deleteById(id);
    }
}