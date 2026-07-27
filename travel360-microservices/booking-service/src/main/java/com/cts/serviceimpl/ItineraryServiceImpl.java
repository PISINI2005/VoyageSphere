package com.cts.serviceimpl;

import com.cts.client.UserClient;
import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.AddBookingDTO;
import com.cts.dto.CreateItineraryDTO;
import com.cts.dto.ItineraryResponseDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Itinerary;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.UserNotFoundException;
import com.cts.mapper.ItineraryMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.ItineraryRepository;
import com.cts.service.AuditLogService;
import com.cts.service.ItineraryService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ItineraryServiceImpl implements ItineraryService {

	private final ItineraryRepository itineraryRepo;
	private final BookingRepository bookingRepo;
	private final UserClient userClient;
	private final AuthenticatedUserProvider authUser;
	private final AuditLogService auditLogService;
	private final ItineraryMapper itineraryMapper;

	@Override
	@Transactional
	public ItineraryResponseDTO createItinerary(CreateItineraryDTO dto) {

		log.info("Creating itinerary for userId: {}", dto.getUserId());

		// Security: Users can only create itineraries for themselves (TRAVEL_AGENT can create for anyone)
		authUser.assertCanActAs(dto.getUserId());

		UserResponseDTO user = fetchUser(dto.getUserId());

		if (!dto.getEndDate().isAfter(dto.getStartDate())) {
			log.error("Invalid date range: endDate {} is not after startDate {}", dto.getEndDate(), dto.getStartDate());
			throw new InvalidBookingException("End date must be after start date");
		}

		Itinerary itinerary = Itinerary.builder().tripName(dto.getTripName()).description(dto.getDescription())
				.startDate(dto.getStartDate()).endDate(dto.getEndDate()).createdAt(LocalDateTime.now()).userId(user.getUserId())
				.bookings(new ArrayList<>()) // Initialize list to prevent NullPointerExceptions
				.build();

		itinerary = itineraryRepo.save(itinerary);
		auditLogService.logAction(AuditActions.CREATE_ITINERARY, AuditEntity.ITINERARY, itinerary.getItineraryId(), currentUserId(), LogType.INFO);

		log.info("Itinerary created successfully with ID: {}", itinerary.getItineraryId());

		return itineraryMapper.toResponse(itinerary);
	}

	@Override
	@Transactional
	public ItineraryResponseDTO addBookingToItinerary(AddBookingDTO dto) {

		log.info("Adding bookingId: {} to itineraryId: {}", dto.getBookingId(), dto.getItineraryId());

		Itinerary itinerary = itineraryRepo.findById(dto.getItineraryId())
				.orElseThrow(() -> {
					log.error("Itinerary not found with id {}", dto.getItineraryId());
					return new ResourceNotFoundException("Itinerary not found");
				});

		Booking booking = bookingRepo.findById(dto.getBookingId())
				.orElseThrow(() -> {
					log.error("Booking not found with id {}", dto.getBookingId());
					return new ResourceNotFoundException("Booking not found");
				});

		if (!booking.getUserId().equals(itinerary.getUserId())) {
			log.error("Booking {} does not belong to the owner of itinerary {}", dto.getBookingId(),
					dto.getItineraryId());
			throw new InvalidBookingException("Booking does not belong to the itinerary owner");
		}

		if (booking.getItinerary() != null) {
			log.error("Booking {} already belongs to an itinerary", dto.getBookingId());
			throw new InvalidBookingException("Booking already belongs to an itinerary");
		}

		// Synchronize the bidirectional relationship in-memory
		booking.setItinerary(itinerary);
		if (itinerary.getBookings() == null) {
			itinerary.setBookings(new ArrayList<>());
		}
		itinerary.getBookings().add(booking);

		bookingRepo.save(booking);
		auditLogService.logAction(AuditActions.ADD_BOOKING_TO_ITINERARY, AuditEntity.ITINERARY, itinerary.getItineraryId(), currentUserId(), LogType.INFO);

		log.info("Booking {} added successfully to itinerary {}", dto.getBookingId(), dto.getItineraryId());

		return itineraryMapper.toResponse(itinerary);
	}

	@Override
	@Transactional
	public ItineraryResponseDTO removeBookingFromItinerary(AddBookingDTO dto) {

		log.info("Removing bookingId: {} from itineraryId: {}", dto.getBookingId(), dto.getItineraryId());

		Itinerary itinerary = itineraryRepo.findById(dto.getItineraryId())
				.orElseThrow(() -> {
					log.error("Itinerary not found with id {}", dto.getItineraryId());
					return new ResourceNotFoundException("Itinerary not found");
				});

		Booking booking = bookingRepo.findById(dto.getBookingId())
				.orElseThrow(() -> {
					log.error("Booking not found with id {}", dto.getBookingId());
					return new ResourceNotFoundException("Booking not found");
				});

		if (booking.getItinerary() == null
				|| !booking.getItinerary().getItineraryId().equals(itinerary.getItineraryId())) {
			log.error("Booking {} does not belong to itinerary {}", dto.getBookingId(), dto.getItineraryId());
			throw new InvalidBookingException("Booking does not belong to this itinerary");
		}

		// Synchronize the bidirectional relationship in-memory during removal
		booking.setItinerary(null);
		if (itinerary.getBookings() != null) {
			itinerary.getBookings().remove(booking);
		}

		bookingRepo.save(booking);
		auditLogService.logAction(AuditActions.REMOVE_BOOKING_FROM_ITINERARY, AuditEntity.ITINERARY, itinerary.getItineraryId(), currentUserId(), LogType.INFO);

		log.info("Booking {} removed successfully from itinerary {}", dto.getBookingId(), dto.getItineraryId());

		return itineraryMapper.toResponse(itinerary);
	}

	@Override
	public List<ItineraryResponseDTO> getUserItineraries(Long userId) {

		log.info("Fetching itineraries for userId: {}", userId);

		authUser.assertCanActAs(userId);

		List<Itinerary> list = itineraryRepo.findByUserId(userId);

		log.info("Total itineraries fetched for userId {}: {}", userId, list.size());

		return list.stream().map(itineraryMapper::toResponse).toList();
	}

	@Override
	public List<ItineraryResponseDTO> getMyItineraries() {
		return getUserItineraries(authUser.current().getUserId());
	}

	@Override
	public ItineraryResponseDTO getItineraryById(Long itineraryId, Long userId) {

		log.info("Fetching itineraryId: {} for userId: {}", itineraryId, userId);

		Itinerary itinerary = itineraryRepo.findById(itineraryId)
				.orElseThrow(() -> {
					log.error("Itinerary not found with id {}", itineraryId);
					return new ResourceNotFoundException("Itinerary not found");
				});

		authUser.assertCanActAs(itinerary.getUserId());

		return itineraryMapper.toResponse(itinerary);
	}

	@Override
	@Transactional
	public ItineraryResponseDTO updateItinerary(Long itineraryId, CreateItineraryDTO dto) {

		log.info("Updating itineraryId: {}", itineraryId);

		Itinerary itinerary = itineraryRepo.findById(itineraryId)
				.orElseThrow(() -> {
					log.error("Itinerary not found with id {}", itineraryId);
					return new ResourceNotFoundException("Itinerary not found");
				});

		authUser.assertCanActAs(itinerary.getUserId());

		if (!dto.getEndDate().isAfter(dto.getStartDate())) {
			log.error("Invalid date range: endDate {} is not after startDate {}", dto.getEndDate(), dto.getStartDate());
			throw new InvalidBookingException("End date must be after start date");
		}

		log.debug("Updating itinerary details for ID: {}", itineraryId);

		itinerary.setTripName(dto.getTripName());
		itinerary.setDescription(dto.getDescription());
		itinerary.setStartDate(dto.getStartDate());
		itinerary.setEndDate(dto.getEndDate());

		itinerary = itineraryRepo.save(itinerary);
		auditLogService.logAction(AuditActions.UPDATE_ITINERARY, AuditEntity.ITINERARY, itinerary.getItineraryId(), currentUserId(), LogType.INFO);

		log.info("Itinerary updated successfully with ID: {}", itineraryId);

		return itineraryMapper.toResponse(itinerary);
	}

	@Override
	@Transactional
	public void deleteItinerary(Long itineraryId, Long userId) {

		log.info("Deleting itineraryId: {} for userId: {}", itineraryId, userId);

		Itinerary itinerary = itineraryRepo.findById(itineraryId)
				.orElseThrow(() -> {
					log.error("Itinerary not found with id {}", itineraryId);
					return new ResourceNotFoundException("Itinerary not found");
				});

		authUser.assertCanActAs(itinerary.getUserId());

		List<Booking> bookings = bookingRepo.findByItineraryItineraryId(itineraryId);
		log.debug("Detaching {} bookings from itinerary {}", bookings.size(), itineraryId);
		bookings.forEach(booking -> booking.setItinerary(null));
		bookingRepo.saveAll(bookings);

		itineraryRepo.delete(itinerary);
		auditLogService.logAction(AuditActions.DELETE_ITINERARY, AuditEntity.ITINERARY, itineraryId, currentUserId(), LogType.WARN);

		log.info("Itinerary deleted successfully with ID: {}", itineraryId);
	}

	/**
	 * Resolves the itinerary owner via the user-service Feign client, throwing the
	 * same {@link UserNotFoundException} the monolith threw on an empty Optional.
	 */
	private UserResponseDTO fetchUser(Long userId) {
		try {
			UserResponseDTO user = userClient.getUser(userId);
			if (user == null) {
				log.error("User not found with id {}", userId);
				throw new UserNotFoundException("User not found");
			}
			return user;
		} catch (UserNotFoundException e) {
			throw e;
		} catch (Exception e) {
			log.error("User not found with id {}", userId);
			throw new UserNotFoundException("User not found");
		}
	}

	private Long currentUserId() {
		AuthenticatedUser caller = authUser.currentOrNull();
		return caller != null ? caller.getUserId() : null;
	}
}
