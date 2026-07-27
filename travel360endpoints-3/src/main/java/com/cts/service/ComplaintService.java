package com.cts.service;

import java.util.List;

import com.cts.dto.ComplaintRequestDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.ComplaintStatusUpdateDTO;
import com.cts.enums.ComplaintStatus;

public interface ComplaintService {

    ComplaintResponseDTO createComplaint(ComplaintRequestDTO dto);

    List<ComplaintResponseDTO> getMyComplaints();

    List<ComplaintResponseDTO> getComplaints(ComplaintStatus status);

    ComplaintResponseDTO getComplaintById(Long complaintId);

    ComplaintResponseDTO updateStatus(Long complaintId, ComplaintStatusUpdateDTO dto);
}
