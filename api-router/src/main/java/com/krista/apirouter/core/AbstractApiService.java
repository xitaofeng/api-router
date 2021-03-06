package com.krista.apirouter.core;

import com.krista.apirouter.exception.ApiException;
import com.krista.apirouter.request.ApiParam;
import com.krista.apirouter.response.ApiResponseCode;
import com.krista.apirouter.utils.*;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 抽象Api服务，使用模板模式设计
 */
public abstract class AbstractApiService<TRequest, TResponse> implements ApiService {
    /**
     * 模板方法
     *
     * @param request
     * @return
     */
    protected abstract TResponse handle(TRequest request) throws ApiException;

    @Override
    public ApiParam execute(ApiParam param) throws ApiException {
        Map body = param.getBody();
        if (body == null || body.isEmpty()) {
            throw new ApiException(ApiResponseCode.RequestBodyIsEmpty);
        }

        String apiKey = ApiUtil.apiWithVersion(param.getHead().getModule(),
                param.getHead().getApiNo(), param.getHead().getVersion());
        TRequest request = (TRequest) BeanMapper.map(body, LocalCache.getRequestParamType(apiKey));

        // JSR -303验证参数
        ValidatorUtil.valid(request);

        TResponse responseBody = handle(request);

        ApiParam response = new ApiParam();
        BeanMapper.copy(param, response);
        if (!(responseBody instanceof Void)) {
            response.setBody(BeanMapper.map(responseBody, Map.class));
        } else {
            response.setBody(null);
        }
        return response;
    }
}