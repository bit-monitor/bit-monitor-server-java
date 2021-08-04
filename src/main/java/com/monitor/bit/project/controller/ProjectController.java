package com.monitor.bit.project.controller;

import com.monitor.bit.auth.annotation.AuthIgnore;
import com.monitor.bit.common.api.ResponseResultBase;
import com.monitor.bit.project.dto.ProjectDTO;
import com.monitor.bit.project.entity.ProjectEntity;
import com.monitor.bit.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;

@RestController
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 新增
     *
     * @param request       request
     * @param projectDTO    projectDTO
     * @param bindingResult bindingResult
     * @return Object
     */
    @RequestMapping(value = "/project/add", method = RequestMethod.PUT)
    public Object add(HttpServletRequest request, @Valid ProjectDTO projectDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ObjectError objectError : bindingResult.getAllErrors()) {
                stringBuilder.append(objectError.getDefaultMessage()).append(",");
            }
            throw new ValidationException(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        } else {
            try {
                return ResponseResultBase.getResponseResultBase(projectService.add(request, projectDTO));
            } catch (Exception e) {
                return ResponseResultBase.getErrorResponseResult(e);
            }
        }
    }

    /**
     * 更新
     *
     * @param request request
     * @return Object
     */
    @RequestMapping(value = "/project/update", method = RequestMethod.POST)
    public Object update(HttpServletRequest request) {
        try {
            return ResponseResultBase.getResponseResultBase(projectService.update(request));
        } catch (Exception e) {
            return ResponseResultBase.getErrorResponseResult(e);
        }
    }

    /**
     * 查询
     *
     * @param request request
     * @return Object
     */
    @RequestMapping(value = "/project/get", method = RequestMethod.GET)
    public Object get(HttpServletRequest request) {
        try {
            return ResponseResultBase.getResponseResultBase(projectService.get(request));
        } catch (Exception e) {
            return ResponseResultBase.getErrorResponseResult(e);
        }
    }

    /**
     * 查询-根据项目标识
     *
     * @param request request
     * @return Object
     */
    @AuthIgnore
    @RequestMapping(value = "/project/getByProjectIdentifier", method = RequestMethod.GET)
    public Object getByProjectIdentifier(HttpServletRequest request) {
        try {
            return ResponseResultBase.getResponseResultBase(projectService.getProjectByProjectIdentifier(request));
        } catch (Exception e) {
            return ResponseResultBase.getErrorResponseResult(e);
        }
    }

    /**
     * 删除
     *
     * @return Object
     */
    @RequestMapping(value = "/project/delete/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable("id") Long id) {
        try {
            return ResponseResultBase.getResponseResultBase(projectService.delete(id));
        } catch (Exception e) {
            return ResponseResultBase.getErrorResponseResult(e);
        }
    }
}
