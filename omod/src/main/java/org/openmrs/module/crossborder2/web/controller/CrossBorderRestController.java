package org.openmrs.module.crossborder2.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.module.crossborder2.openhim.CbPatientService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * Exposes REST queries for the cross border module
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/kemrcrossborder")
@Authorized
public class CrossBorderRestController extends BaseRestController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
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
