package com.monitor.bit.user.controller;

import com.monitor.bit.user.entity.UserRegisterRecordEntity;
import com.monitor.bit.auth.annotation.AuthIgnore;
import com.monitor.bit.user.service.UserRegisterRecordService;
import com.monitor.bit.common.api.ResponseResultBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;

@RestController
public class UserRegisterRecordController {
    @Autowired
    private UserRegisterRecordService userRegisterRecordService;

    /**
     * 新增
     *
     * @param userRegisterRecordEntity userRegisterRecordEntity
     * @param bindingResult            bindingResult
     * @return Object Object
     */
    @AuthIgnore
    @RequestMapping(value = "/userRegisterRecord/add", method = RequestMethod.PUT)
    public Object add(@Valid UserRegisterRecordEntity userRegisterRecordEntity, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ObjectError objectError : bindingResult.getAllErrors()) {
                stringBuilder.append(objectError.getDefaultMessage()).append(",");
            }
            throw new ValidationException(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
        } else {
            return ResponseResultBase.getResponseResultBase(userRegisterRecordService.add(userRegisterRecordEntity));
        }
    }

    /**
     * 审批
     *
     * @param request request
     * @return Object
     */
    @RequestMapping(value = "/userRegisterRecord/audit", method = RequestMethod.POST)
    public Object audit(HttpServletRequest request) {
        try {
            return ResponseResultBase.getResponseResultBase(userRegisterRecordService.audit(request));
        } catch (Exception e) {
            return ResponseResultBase.getErrorResponseResult(e);
        }
    }

    /**
     * 多条件查询
     *
     * @param request request
     * @return Object
     */
    @RequestMapping(value = "/userRegisterRecord/get", method = RequestMethod.GET)
    public Object get(HttpServletRequest request) {
        return ResponseResultBase.getResponseResultBase(userRegisterRecordService.get(request));
    }
}
