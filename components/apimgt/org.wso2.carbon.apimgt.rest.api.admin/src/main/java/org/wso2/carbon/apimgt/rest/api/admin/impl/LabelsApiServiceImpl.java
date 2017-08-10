package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.admin.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.LabelMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;


public class LabelsApiServiceImpl extends LabelsApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelsApiServiceImpl.class);

    /**
     * Gets all labels
     * @param accept Accept header value
     * @param request ms4j request object
     * @return  a list of label objects
     * @throws NotFoundException
     */
    @Override
    public Response labelsGet(String labelId, String accept , Request request) throws NotFoundException {
        List<Label>  labels = new ArrayList<>();
        try{
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            //get all labels
            if(labelId == null) {
                labels = apiMgtAdminService.getLabels();
            }else{
                 Label label = apiMgtAdminService.getLabelByID(labelId);
                 labels.add(label);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving all labels";
            ErrorHandler errorHandler = ExceptionCodes.LABEL_EXCEPTION;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler);
            log.error(errorMessage);
            return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.status(Response.Status.OK).entity(LabelMappingUtil.fromLabelArrayToListDTO(labels)).build();
    }

    /**
     * Delete label by label id
     *
     * @param labelId           Id of the label
     * @param request           msf4j request object
     * @return 200 OK if the operation is successful
     * @throws NotFoundException If failed to find the particular resource
     */
    @Override
    public Response labelsDelete(String labelId, Request request) throws NotFoundException {
        try {
            if (labelId != null) {
                APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
                apiMgtAdminService.deleteLabel(labelId);
            } else {
                //mandatory parameters not provided
                String errorMessage = "Label Id parameter should be provided";
                ErrorHandler errorHandler = ExceptionCodes.PARAMETER_NOT_PROVIDED;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler);
                log.error(errorMessage);
                return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting the label [labelId] " + labelId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

        return Response.ok().build();
    }

    /**
     *  Update the label
     * @param body     The body of the label with fields to be modified
     * @param contentType The content type of the body
     * @param request     The ms4j request object
     * @return  200 OK response.
     * @throws NotFoundException
     */
    @Override
    public Response labelsPut(LabelDTO body, String contentType, String labelId, Request request) throws
            NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            body.labelUUID(labelId);
            apiMgtAdminService.updateLabel(LabelMappingUtil.fromDTOTLabel(body));
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding label, label name: " + body.getName();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Adds a label
     * @param body        The label details of the label to be added
     * @param contentType Content type of the body
     * @param request     ms4j request obect
     * @return  the label object that was added, with the label ID and label name.
     * @throws NotFoundException
     */
    @Override
    public Response labelsPost(LabelDTO body, String contentType, Request request) throws NotFoundException {

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            if (body != null && body.getLabelTypeName() != null) {
                if (body.getLabelTypeName().equalsIgnoreCase("STORE")) {
                    body.setAccessUrls(new ArrayList<>());
                }
                Label labelAdded = apiMgtAdminService.addLabel(LabelMappingUtil.fromDTOTLabel(body));
                return Response.status(Response.Status.CREATED).entity(LabelMappingUtil.fromLabelToDTO(labelAdded)).build();

            } else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding label, label name: " + body.getName();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

}
