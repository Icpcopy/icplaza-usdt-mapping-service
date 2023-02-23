package com.icplaza.mapping.dao;

import com.icplaza.mapping.model.OrderInLogModel;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderLogDAO {
    @Insert("insert into order_log (order_no,address,phone,txhash,txvalue,create_time) values (#{orderNo}, #{address},#{phone},#{txhash},#{txvalue},#{createTime,jdbcType=TIMESTAMP})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderInLogModel record);

    @Select("select * from order_log where phone=#{phone} limit 1")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
    })
    OrderInLogModel selectModelByPhone(String phone);

    @Select("select * from order_log where txhash=#{hash} limit 1")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
    })
    OrderInLogModel selectModelByHash(String hash);

    @Select("select * from order_log where phone=#{phone}")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
    })
    List<OrderInLogModel> selectModelsByPhone(String phone);

    @Select("select * from order_log where address=#{address} limit 1")
    @Results({
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "create_time", property = "createTime"),
    })
    OrderInLogModel selectModelByAddress(String address);

    @Select("select * from order_log where address=#{address}")
    @Results({
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "create_time", property = "createTime"),
    })
    List<OrderInLogModel> selectModelsByAddress(String address);

    @Select("select sum(txvalue) from order_log")
    BigDecimal sum();

    /*未推送数据列表：每次限制100条*/
    @Select("select * from order_log where pushed=0 limit 100")
    @Results({
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "create_time", property = "createTime"),
    })
    List<OrderInLogModel> getUnPushedList();

    @Update("update order_log set pushed=1 where id=#{id}")
    boolean setPushed(Integer id);

    @Select("select * from order_log where pushed=0 order by id desc limit 10")
    @Results({
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "create_time", property = "createTime"),
    })
    List<OrderInLogModel> getLast10UnPushedList();

}
