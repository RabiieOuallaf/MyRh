package com.project.MyRh.Services;


import com.project.MyRh.DTO.ApplicantDto;
import com.project.MyRh.DTO.ApplicationsDto;
import com.project.MyRh.DTO.CompanyDto;
import com.project.MyRh.DTO.JobOfferDto;
import com.project.MyRh.DTO.Request.ApplicantRequest;
import com.project.MyRh.DTO.Request.ApplicationToUpdate;
import com.project.MyRh.DTO.Request.ApplicationsRequest;
import com.project.MyRh.Exceptions.Exception.AlreadyExisting;
import com.project.MyRh.Exceptions.Exception.NotFound;
import com.project.MyRh.Exceptions.Exception.OperationFailed;
import com.project.MyRh.Mappers.Mapper;
import com.project.MyRh.Models.Entities.Applications;
import com.project.MyRh.Models.Entities.Company;
import com.project.MyRh.Repositories.ApplicationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationsService {
    private final ApplicationsRepository applicationsRepository;
    private final ApplicantService applicantService;
    private final CompanyService companyService;
    private final JobOfferService jobOfferService;
    private final Mapper<Applications, ApplicationsDto> jobApplicantsMapper;
    @Autowired
    public ApplicationsService(ApplicationsRepository applicationsRepository,
                               CompanyService companyService,
                               ApplicantService applicantService,
                               JobOfferService jobOfferService,
                               Mapper<Applications, ApplicationsDto> jobApplicantsMapper)
    {
        this.applicationsRepository = applicationsRepository;
        this.applicantService = applicantService;
        this.jobApplicantsMapper = jobApplicantsMapper;
        this.companyService = companyService;
        this.jobOfferService = jobOfferService;

    }

    public ApplicationsDto findById(Integer id) {
        Optional<Applications> jobApplicants = applicationsRepository.findById(id);
        if (jobApplicants.isPresent()){
            return jobApplicantsMapper.mapTo(jobApplicants.get());
        }else {
            throw new NotFound("JobApplicants not found");
        }
    }

    public List<ApplicationsDto> findByJobOffer(Integer jobOffer_id){
        return applicationsRepository.findByJobOffer_Id(jobOffer_id).stream().map(jobApplicantsMapper::mapTo).toList();
    }

    public ApplicationsDto findByApplicant_IdAndJobOffer_Id(Integer applicant_id, Integer jobOffer_id) {
        Optional<Applications> application = applicationsRepository.findByApplicant_IdAndJobOffer_Id(applicant_id, jobOffer_id);
        return application.map(jobApplicantsMapper::mapTo).orElse(null);
    }

    public List<ApplicationsDto> findByApplicationsCompanyName(String companyEmail) {
        CompanyDto foundCompany = companyService.getByEmail(companyEmail);
        CompanyDto companyDto = companyService.getByName(foundCompany.getName());
        if(companyDto != null){
            List<JobOfferDto> jobOffersDto = jobOfferService.findJobOfferByCompanyId(companyDto.getId());
            if(jobOffersDto.size() > 0){
                List<ApplicationsDto> applicationsDto = applicationsRepository.findByJobOffer_Id(jobOffersDto.get(0).getId()).stream().map(jobApplicantsMapper::mapTo).toList();
                return applicationsDto;
            }
        }
        if (companyDto != null) {
        } else {
            throw new NotFound("Company not found");
        }
        return null;
    }


    public boolean isApplicationExisting(Integer applicant_id, Integer jobOffer_id){
        return findByApplicant_IdAndJobOffer_Id(applicant_id, jobOffer_id) != null;
    }

    public ApplicationsDto save(ApplicationsRequest applicationsRequest) {
        if (applicantService.getByEmail(applicationsRequest.getEmail()) != null){
            applicationsRequest.setApplicant_id(applicantService.getByEmail(applicationsRequest.getEmail()).getId());
                if (!isApplicationExisting(applicationsRequest.getApplicant_id(), applicationsRequest.getJobOffer_id())){
                    try {
                        Applications application = applicationsRequest.toModel();
                        application.setDate(new Date());
                        application.setStatus("ON HOLD");
                        return jobApplicantsMapper.mapTo(applicationsRepository.save(application));
                    }catch (OperationFailed e){
                        throw new OperationFailed("Failed to create Application!");
                    }
                }else {
                    throw new AlreadyExisting("Applicant already applied to this offer !");
                }

        }else {
            try {
                ApplicantRequest applicantRequest = new ApplicantRequest();
                applicantRequest.setName(applicationsRequest.getName());
                applicantRequest.setEmail(applicationsRequest.getEmail());
                applicantRequest.setPhone(applicationsRequest.getPhone());
                applicantRequest.setAddress("adress");
                applicantRequest.setEducation("education");
                applicantRequest.setExperience("exp");

                if (applicantService.saveApplicant(applicantRequest) != null){
                    try {
                        Applications application = applicationsRequest.toModel();
                        application.setDate(new Date());
                        application.setStatus("ON HOLD");
                        return jobApplicantsMapper.mapTo(applicationsRepository.save(application));
                    }catch (OperationFailed e){
                        throw new OperationFailed("Failed to create Application!");
                    }
                }else {
                    throw new OperationFailed("Failed to create Applicant!");
                }
            }catch (OperationFailed e){
                throw new OperationFailed("Failed to create Application!");
            }
        }

    }

    public ApplicationsDto changeApplicationStatus(ApplicationToUpdate applicationsRequest) {
        ApplicationsDto existingApplication = findByApplicant_IdAndJobOffer_Id(
                applicationsRequest.getApplicant_id(),
                applicationsRequest.getJobOffer_id()
        );

        if (existingApplication != null) {
            Applications application = jobApplicantsMapper.mapFrom(existingApplication);
            application.setStatus(applicationsRequest.getStatus());

            return jobApplicantsMapper.mapTo(applicationsRepository.save(application));
        } else {
            throw new NotFound("Application not found");
        }
    }



}
