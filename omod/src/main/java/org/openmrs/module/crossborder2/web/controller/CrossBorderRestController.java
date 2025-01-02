package org.openmrs.module.crossborder2.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.crossborder2.openhim.CbPatientService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

/**
 * Exposes REST queries for the cross border module
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/kemrcrossborder")
@Authorized
public class CrossBorderRestController extends BaseRestController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
	public static final String REFERRAL_ENCOUNTERTYPE_UUID = "5C6DA02B-51E8-4B3D-BB67-BE8F75C4CCE1";
	
	public static final String SCREENING_ENCOUNTERTYPE_UUID = "6536A8A3-7B77-414D-A0F0-E08A7178FF0F";
	
	@RequestMapping(method = RequestMethod.GET, value = "/searchpatient")
    @ResponseBody
    public List<Patient> searchPatient(HttpServletRequest request, @SpringBean CbPatientService cbPatientService,
                                                @RequestParam(value = "cbId", required = false) String cbId,
                                                @RequestParam(value = "clinicNo", required = false) String clinicNo,
                                                @RequestParam(value = "nationalId", required = false) String nationalId,
                                                @RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "gender", required = false) String gender
    ) {
        HashMap<String,String> searchParams = new HashMap<>();
        if (cbId != null) {
			searchParams.put("cbId", cbId);
		}

		if (clinicNo != null) {
            searchParams.put("clinicNo", clinicNo);
		}

		if (nationalId != null) {
            searchParams.put("nationalId", nationalId);
		}

		if (name != null) {
			searchParams.put("name", name);
		}

		if (gender != null) {
			searchParams.put("gender", gender);
		}
        try {
            List<Patient> patients = cbPatientService.searchPatient(searchParams);
            return patients;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	@RequestMapping(method = RequestMethod.GET, value = "/cbencounter")
	@ResponseBody
	public ArrayList<SimpleObject> cbEncounters(HttpServletRequest request,
	        @RequestParam(value = "fromdate", required = false) String fromDate,
	        @RequestParam(value = "todate", required = false) String toDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		EncounterService encounterService = Context.getEncounterService();
		
		// Define encounter types
		List<EncounterType> cbEncounterTypes = Arrays.asList(
		    encounterService.getEncounterTypeByUuid(REFERRAL_ENCOUNTERTYPE_UUID),
		    encounterService.getEncounterTypeByUuid(SCREENING_ENCOUNTERTYPE_UUID));
		
		try {
			EncounterSearchCriteriaBuilder searchCriteria = new EncounterSearchCriteriaBuilder()
			        .setFromDate(fromDate != null ? formatter.parse(fromDate) : null)
			        .setToDate(toDate != null ? formatter.parse(toDate) : null).setEncounterTypes(cbEncounterTypes)
			        .setIncludeVoided(false);
			ArrayList cbEncounters = new ArrayList<SimpleObject>();
			
			List<Encounter> encounters = encounterService.getEncounters(searchCriteria.createEncounterSearchCriteria());
			for (Encounter encounter : encounters) {
				SimpleObject cbEncounter = new SimpleObject();
				cbEncounter.put("encounterUuid", encounter.getUuid());
				cbEncounter.put("encounterType", encounter.getEncounterType().getName());
				cbEncounter.put("encounterTypeUuid", encounter.getEncounterType().getUuid());
				cbEncounter.put("encounterDatetime", formatDate(encounter.getEncounterDatetime()));
				cbEncounter.put("patientName", encounter.getPatient().getPersonName().getFullName());
				cbEncounter.put("patientUuid", encounter.getPatient().getUuid());
				cbEncounter.put("location", encounter.getLocation().getName());
				//cbEncounter.put("provider", encounter.getProvider().getPersonName().getFullName());
				cbEncounter.put("form", encounter.getForm().getName());
				cbEncounter.put("visit", encounter.getVisit().getVisitType().getName());
				cbEncounters.add(cbEncounter);
				
			}
			return cbEncounters;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid date format. Please use 'yyyy-MM-dd'.");
		}
	}
	
	private String formatDate(Date date) {
		return date == null ? "" : DATE_FORMAT.format(date);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/createpatient")
	@ResponseBody
	public Patient createPatient(HttpServletRequest request, @SpringBean CbPatientService cbPatientService,
	        @RequestParam(value = "patient", required = true) Patient patient) {
		
		if (patient == null) {
			return null;
		}
		try {
			Patient createdPatient = cbPatientService.createPatient(patient);
			return createdPatient;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @see BaseRestController#getNamespace()
	 */
	
	@Override
	public String getNamespace() {
		return "v1/kemrcrossborder";
	}
}
