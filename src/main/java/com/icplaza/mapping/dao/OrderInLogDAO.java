package com.icplaza.mapping.dao;

import com.icplaza.mapping.bo.Condition;
import com.icplaza.mapping.model.OrderInLogModel;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Mapper
public interface OrderInLogDAO {
    String table = "order_in_log";


    @Insert("insert into " + table + " (channel_type,order_no, icplaza, from_0,to_0,value_0,hash_0,hash_status_0,create_time) values (#{channelType},#{orderNo}, #{icplaza},#{from_0}, #{to_0},#{value_0}, #{hash_0},#{hashStatus_0}, #{createTime,jdbcType=TIMESTAMP})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addStep1(OrderInLogModel model);

    @Update("update " + table + " set from_1=#{from_1},to_1=#{to_1},value_1=#{value_1},hash_1=#{hash_1},hash_status_1=#{hashStatus_1},update_time=#{updateTime,jdbcType=TIMESTAMP} where id=#{id}")
    boolean addStep2(OrderInLogModel model);

    @Select("select * from " + table + " where hash_0=#{hash} limit 1")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
    })
    OrderInLogModel selectModelByHash0(String hash);

    @Select("<script>" +
            "SELECT * " +
            "from " + table +
            "<where>" +
            "<if test ='channelType != null'>" +
            " and channel_type = #{channelType}" +
            "</if>" +
            "<if test ='hashStatus != null'>" +
            " and hash_status_1 = #{hashStatus}" +
            "</if>" +
            "<if test ='icplaza != null'>" +
            // " and icplaza = #{icplaza}" +
            " and (icplaza = #{icplaza} or from_0 = #{icplaza} or to_0 = #{icplaza} or from_1 = #{icplaza} or to_1 = #{icplaza})" +
            "</if>" +
            "</where>" +
            "ORDER BY id desc limit #{start},#{pageSize}" +
            "</script>")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
    })
    List<OrderInLogModel> selectWithCondition(String icplaza, Integer channelType, Integer hashStatus, Integer start, Integer pageSize);

    @Select("<script>" +
            "SELECT count(*) " +
            "from " + table +
            "<where>" +
            "<if test ='channelType != null'>" +
            " and channel_type = #{channelType}" +
            "</if>" +
            "<if test ='hashStatus != null'>" +
            " and hash_status_1 = #{hashStatus}" +
            "</if>" +
            "<if test ='icplaza != null'>" +
            // " and icplaza = #{icplaza}" +
            " and (icplaza = #{icplaza} or from_0 = #{icplaza} or to_0 = #{icplaza} or from_1 = #{icplaza} or to_1 = #{icplaza})" +
            "</if>" +
            "</where>" +
            "</script>")
    BigInteger selectCountWithCondition(String icplaza, Integer channelType, Integer hashStatus, Integer start, Integer pageSize);

    /**
     * 第一步成功；第二步未执行或者失败
     */
    @Select("select * from " + table + " where hash_status_0=1 and (hash_1 is null or hash_status_1=2) limit 100")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
    })
    List<OrderInLogModel> step2List();


    @Select("select * from " + table + " where hash_status_0=1 and hash_1 is not null and hash_status_1=0 limit 100")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
    })
    List<OrderInLogModel> step2PendingList();

    @Update("update " + table + " set hash_status_1=#{hashStatus_1} where id=#{id}")
    void updateStep2Status(OrderInLogModel model);

    /**
     * 所有映射入数量
     */
    @Select("select sum(value_0) from " + table + " where hash_status_0=1")
    BigDecimal sum();
}
