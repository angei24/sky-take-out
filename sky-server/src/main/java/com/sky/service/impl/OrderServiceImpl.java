package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;

    //提交订单
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //判断订单是否正确--地址是否为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null)
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        //检查用户收货地址是否超出范围
//        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        //判断订单是否正确--购物车是否为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.size() == 0)
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        //订单表中插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setPhone(addressBook.getPhone());
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCancelReason(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orderMapper.insert(orders);
        //订单详细表中插入多条数据
        List<OrderDetail> detailList = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            detailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(detailList);
        //清空用户的购物车
        shoppingCartMapper.cleanByUserId(userId);
        //封装VO对象
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
        return submitVO;
    }

    //订单支付
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        //调用微信支付接口，生成预支付交易单
        /*JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid()); //微信用户的openid */
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID"))
            throw new OrderBusinessException("该订单已支付");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        return vo;
    }

    //支付成功，修改订单状态
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
        //通过websocket向客户端发送消息-type-orderId-content
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号:"+outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    //用户历史订单查询
    public PageResult pageQuery(Integer page, Integer pageSize, Integer status) {
        //分页查询
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        //查询分页结果
        Page<Orders> ordersPage = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        //为每一条订单查询详细信息
        if (ordersPage != null && ordersPage.getTotal() > 0) {
            for (Orders order : ordersPage) {
                Long orderId = order.getId();
                List<OrderDetail> details = orderDetailMapper.getDetailByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                orderVO.setOrderDetailList(details);
                list.add(orderVO);
            }
        }
        return new PageResult(ordersPage.getTotal(), list);
    }

    //根据订单id查询详细信息
    public OrderVO getDetails(Long id) {
        //查询订单信息
        Orders order = orderMapper.getById(id);
        //查询订单详细信息
        List<OrderDetail> details = orderDetailMapper.getDetailByOrderId(id);
        //封装VO对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    //取消订单
    public void userCancelOrder(Long id) throws Exception {
        Orders orders = orderMapper.getById(id);
        //判断订单是否为空
        if (orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        //判断订单状态
        if (orders.getStatus() > 2)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //更新订单状态
        Orders order = new Orders();
        order.setId(id);
        //待接单状态取消要退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额
            //修改支付状态为退款
            order.setPayStatus(Orders.REFUND);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    //再来一单
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> details = orderDetailMapper.getDetailByOrderId(id);
        //转换为购物车对象
        List<ShoppingCart> shoppingCartList = details.stream().map(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //批量添加到购物车
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    //订单查询
    public PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersPage = orderMapper.adminPageQuery(ordersPageQueryDTO);
        //转换为VO对象
        List<OrderVO> list = getOrderVOList(ordersPage);
        return new PageResult(ordersPage.getTotal(), list);
    }

    //统计各个状态的订单数量
    public OrderStatisticsVO orderStatistics() {
        //待接单-待派送-派送中-各个状态的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliverInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        //封装VO对象
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliverInProgress);
        return orderStatisticsVO;
    }

    //接单
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    //拒单
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //用户支付拒单要退款
        if (orders.getPayStatus() == Orders.PAID) {
            weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
        }
        //更新订单信息
        Orders order = new Orders();
        order.setId(orders.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    //取消订单
    public void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        //用户已支付要付款
        if (orders.getPayStatus() == Orders.PAID) {
            weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
        }
        //更新订单信息
        Orders order = new Orders();
        order.setId(orders.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    //派送订单
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (!orders.getStatus().equals(Orders.CONFIRMED))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //更新订单信息
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    //完成订单
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS))
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //更新订单信息
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    //用户催单
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        //构建map对象
        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号:"+orders.getNumber());
        String json = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> list = new ArrayList<>();
        List<Orders> result = page.getResult();
        if (!result.isEmpty()) {
            for (Orders order : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                String orderDishes = getOrderDishesStr(order);
                orderVO.setOrderDishes(orderDishes);
                list.add(orderVO);
            }
        }
        return list;
    }

    private String getOrderDishesStr(Orders order) {
        //查询订单详细信息
        List<OrderDetail> details = orderDetailMapper.getDetailByOrderId(order.getId());
        //把每一份菜品和数量分别拼接字符串
        List<String> strings = details.stream().map(item -> {
            String orderDish = item.getName() + "*" + item.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        //拼接所有字符串
        return String.join("", strings);
    }

    //解析用户收货地址是超出范围
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);
        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }
        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;
        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }
        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;
        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");
        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }
        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");
        //配送距离超过5000米
        if(distance > 5000)
            throw new OrderBusinessException("超出配送范围");
    }
}
