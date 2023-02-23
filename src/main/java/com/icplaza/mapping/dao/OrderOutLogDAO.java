package com.icplaza.mapping.dao;

import com.icplaza.mapping.model.OrderInLogModel;
import com.icplaza.mapping.model.OrderOutLogModel;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Mapper
public interface OrderOutLogDAO {
    String table = "order_out_log";


    @Insert("insert into " + table + " (channel_type,order_no,icplaza,from_0,to_0,value_0,hash_0,hash_status_0,receiver,create_time) values (#{channelType},#{orderNo}, #{icplaza},#{from_0}, #{to_0},#{value_0}, #{hash_0},#{hashStatus_0}, #{receiver},#{createTime,jdbcType=TIMESTAMP})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addStep1(OrderOutLogModel model);

    @Update("update " + table + " set from_1=#{from_1},to_1=#{to_1},value_1=#{value_1},fee_1=#{fee_1},hash_1=#{hash_1},hash_status_1=0,update_time=#{updateTime,jdbcType=TIMESTAMP} where id=#{id}")
    boolean addStep2(OrderOutLogModel model);

    @Update("update " + table + " set from_1=#{from_1},to_1=#{to_1},value_1=#{value_1},fee_1=#{fee_1},hash_1=#{hash_1},hash_status_1=1,hash_status_2=1,update_time=#{updateTime,jdbcType=TIMESTAMP} where id=#{id}")
    boolean addStep2Success(OrderOutLogModel model);

    @Update("update " + table + " set from_1=#{from_1},to_1=#{to_1},value_1=#{value_1},hash_1=#{hash_1},refund=1,hash_status_1=0,update_time=#{updateTime,jdbcType=TIMESTAMP} where id=#{id}")
    boolean addStep2Refund(OrderOutLogModel model);

    @Update("update " + table + " set from_2=#{from_1},to_2=#{to_1},value_2=#{value_2},hash_2=#{hash_2},hash_status_2=0,burn_time=#{burnTime,jdbcType=TIMESTAMP} where id=#{id}")
    boolean addStep3(OrderOutLogModel model);

    /**
     * 第二步成功；第三步未执行或者执行失败；并且不是回退;并且第三不的哈希不是成功
     */
    @Select("select * from " + table + " where hash_status_1=1 and (hash_2 is null or hash_status_2=2)  and hash_status_2!=1  and refund=0 limit 100")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "hash_status_2", property = "hashStatus_2"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "burn_time", property = "burnTime"),
    })
    List<OrderOutLogModel> getBurnLockPending();


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
            @Result(column = "hash_status_2", property = "hashStatus_2"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "burn_time", property = "burnTime"),
    })
    List<OrderOutLogModel> selectWithCondition(String icplaza, Integer channelType, Integer hashStatus, Integer start, Integer pageSize);

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
     * 第一步成功；第二步未执行，或者执行失败
     */
    @Select("select * from " + table + " where hash_status_0=1 and (hash_1 is null or hash_status_1=2) limit 100")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "hash_status_2", property = "hashStatus_2"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "burn_time", property = "burnTime"),
    })
    List<OrderOutLogModel> step2List();


    @Select("select * from " + table + " where hash_status_0=1 and hash_1 is not null and hash_status_1=0 limit 100")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "hash_status_2", property = "hashStatus_2"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "burn_time", property = "burnTime"),
    })
    List<OrderOutLogModel> step2PendingList();

    @Update("update " + table + " set hash_status_1=#{hashStatus_1} where id=#{id}")
    void updateStep2Status(OrderOutLogModel model);


    @Select("select * from " + table + " where hash_status_1=1 and hash_2 is not null and hash_status_2=0 limit 100")
    @Results({
            @Result(column = "channel_type", property = "channelType"),
            @Result(column = "order_no", property = "orderNo"),
            @Result(column = "hash_status_0", property = "hashStatus_0"),
            @Result(column = "hash_status_1", property = "hashStatus_1"),
            @Result(column = "hash_status_2", property = "hashStatus_2"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime"),
            @Result(column = "burn_time", property = "burnTime"),
    })
    List<OrderOutLogModel> step3PendingList();


    @Update("update " + table + " set hash_status_2=#{hashStatus_2} where id=#{id}")
    void updateStep3Status(OrderOutLogModel model);


    @Update("update " + table + " set hash_status_2=#{status}  where id=#{id}")
    void updateStep3StatusByAPI(BigInteger id, Integer status);

    @Update("update " + table + " set hash_status_0=0 where id=#{id} and hash_status_0=#{status}")
    void setPending(BigInteger id, Integer status);

    @Update("update " + table + " set hash_status_0=1 where id=#{id} and hash_status_0=#{status}")
    void setSuccess(BigInteger id, Integer status);

    /**
     * 映射出总量
     */
    @Select("select sum(value_1) from " + table + " where refund=0 and hash_status_0=1 and value_0>0")
    BigDecimal sum();

    /**
     * 映射出手续费总量
     */
    @Select("select sum(fee_1) from " + table + " where refund=0 and hash_status_0=1")
    BigDecimal fee();

    /**
     * 销毁总量
     */
    @Select("select sum(value_2) from " + table + " where refund=0 and hash_status_2=1")
    BigDecimal burn();
}
