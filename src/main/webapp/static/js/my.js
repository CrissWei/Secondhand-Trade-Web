//一加载页面就进行的操作
$(document).ready(function () {

    //全部图片
    $("img").addClass("img-responsive");
    //鼠标悬停更换返回顶部图片
    $(".imgdiv").mouseover(function () {
        $(".imgsrc").attr("src", "/static/images/top4.png");
    });
    $(".imgdiv").mouseout(function () {
        $(".imgsrc").attr("src", "/static/images/top2.png");
    });
});

//倒计时时间,用于再次获取验证码
let countdown = 60;

//点击获取验证码按钮后,使按钮不可用,60秒时恢复可用
function setTime(val, type) {
    let existEmail;
    if (type === 1) {
        let email = $("#emailRegister").val();
        if (email == null || email === "") {
            return false;
        }
    } else if (type === 2) {
        let email = $("#emailResetPassword").val();
        if (email == null || email === "") {
            return false;
        }
        //找回密码时需要先确认数据库中是否存在该邮箱
        $.ajax({
            url: "/user/existEmail",
            type: "get",
            data: {email: email},
            async: false,
            success: function (result) {
                existEmail = !!result.success;
            }
        });
        if (existEmail !== true) {
            return false;
        }
    }
    if (countdown === 0) {
        val.removeAttribute("disabled");
        val.className = 'btn-warning btn btn-sm';
        val.value = "Get verify code";//发验证码
        countdown = 60;
        return false;
    } else {
        val.setAttribute("disabled", true);
        val.className = 'btn-default btn btn-sm disabled';
        val.value = "Resend(" + countdown + ")";//重新发验证码
        countdown--;
    }
    setTimeout(function () {
        setTime(val);
    }, 1000);
}

//返回顶部图标出现或者消失
$(function () {
    $(function () {
        $(window).scroll(function () {
            if ($(window).scrollTop() > 100) {
                $("#gotop").fadeIn(1000);//一秒渐入动画
            } else {
                $("#gotop").fadeOut(1000);//一秒渐隐动画
            }
        });

        $("#gotop").click(function () {
            $('body,html').animate({scrollTop: 0}, 1000);
        });
    });
});

//检验用户名是否存在
function checkUserName(userName) {
    if (userName === "") {
        $("#userNameFail").html("Username cannot be empty ");//用户名不能为空
        $("#userNameSuccess").html("");
        $("#userName").focus();
        return;
    }
    $.post("/user/existUserWithUserName", {userName: userName},
        function (result) {
            if (result.success) {
                $("#userNameFail").html("Username exist");//用户名已存在
                $("#userNameSuccess").html("");
                $("#userName").focus();
            } else {
                $("#userNameSuccess").html("Username can use");//用户名可以使用!
                $("#userNameFail").html("");
            }
        }
    );
}

//检验邮箱是否存在--注册时
function checkEmail(email) {
    if (email === "") {
        $("#emailFail").html("Email cannot be empty");//邮箱不能为空
        $("#emailSuccess").html("");
        $("#emailRegister").focus();
        return;
    }
    $.post("/user/existEmail", {email: email},
        function (result) {
            if (result.success) {
                $("#emailFail").html("Email exist");
                $("#emailSuccess").html("");
                $("#emailRegister").focus();
            } else {
                $("#emailFail").html("");
                $("#emailSuccess").html("Email can use");
            }
        }
    );
}

//向后端发送获取验证码请求,参数为验证码类型(type=1时为注册,type=2时为找回密码)
function getVerificationCode(type) {
    let email
    if (type === 1) {
        email = $("#emailRegister").val();
    } else if (type === 2) {
        email = $("#emailResetPassword").val();
        //找回密码时需要先确认数据库中是否存在该邮箱
        let existEmail;
        $.ajax({
            url: "/user/existEmail",
            type: "get",
            data: {email: email},
            async: false,
            success: function (result) {
                existEmail = result.success;
            }
        });
        if (existEmail === false) {
            alert("New email")//你输入的邮箱地址没有在本站注册!
            return false;
        }
    }
    if (email == null || email === "") {
        alert("Email cannot be empty ");//邮箱不能为空!!
        return false;
    }
    $.ajax({
        url: "/user/getVerificationCode",
        type: "get",
        data: {type: type, email: email},
        success: function (result) {
            if (result.success) {
                if (type === 1) {
                    alert("Please check the verification code")//注册的验证码已经发送到你的邮箱,请注意查收!!
                } else if (type === 2) {
                    alert("Please check the verification code")//找回密码的验证码已经发送到你的邮箱,请注意查收!!
                }
            }
        }
    });
}

//注册时验证
function checkRegisterValue() {

    let password = $("#passwordRegister").val();
    let password2 = $("#password2Register").val();
    let registerCode = $("#registerCode").val();
    let imageCode;
    if (password.length < 6) {
        alert("The length of the password need more than 5 ");//密码长度要大于5!
        return false;
    }
    if (password !== password2) {
        alert("The password is not the same like previous one");//密码和确认密码不相同,请重新输入!
        return false;
    }
    $.ajax({
        url: "/user/getRegisterCode",
        type: "get",
        async: false,
        success: function (result) {
            if (result.success) {
                imageCode = result.imageCode;
            }
        }
    });
    if (registerCode !== imageCode) {
        alert("Verification code wrong");//验证码不正确,请重新输入!
        return false;
    }
    return true;
}

function logoutUser() {
    if (confirm("Are you sure logout? ")) {//您确定要退出登录吗?
        window.location.href = "/user/logout";
    }
}

//找回密码时验证
function checkResetPasswordValue() {

    let password = $("#passwordResetPassword").val();
    let password2 = $("#password2ResetPassword").val();
    let resetPasswordCode = $("#resetPasswordCode").val();
    let imageCode;
    if (password.length < 6) {
        alert("The length of the password need more than 5");//密码长度要大于5
        return false;
    }
    if (password !== password2) {
        alert("The password is not the same like previous one");
        return false;
    }
    $.ajax({
        url: "/user/getResetPasswordCode",
        type: "get",
        async: false,
        success: function (result) {
            if (result.success) {
                imageCode = result.imageCode;
            }
        }
    });
    alert(resetPasswordCode + "," + imageCode)
    if (resetPasswordCode !== imageCode) {
        alert("Verification code wrong, please try again ");
        return false;
    }
    return true;
}

//修改个人信息时验证
function checkModifyValue() {
    let password = $("#passwordModify").val();
    let password2 = $("#password2Modify").val();
    if (password !== password2) {
        alert("The password is not the same like previous one");
        return false;
    }
    return true;
}

//留言时验证
function checkContactValue() {
    let userId = $("#userId").val();
    if (userId == null || userId === '') {
        alert("Your login status has expired, please login again ");
        return false;
    }
    return true;
}

//删除留言
function deleteContact(id) {
    if (confirm("Are you sure to delete this? ")) {
        window.location.href = "/contact/delete?id=" + id;
    }
}

//查看留言详情
function seeContactDetails(id) {
    $.ajax({
        url: "/contact/findById",
        type: "get",
        data: {id: id},
        success: function (result) {
            if (result.success) {
                $("#contactId").html(result.contact.id);
                $("#contactTime").html(result.contact.time);
                $("#contactContent").html(result.contact.content);
                if (result.contact.reply == null) {
                    $("#contactReply").html("<span style='color: red'>Not reply</span>");
                } else {
                    $("#contactReply").html(result.contact.reply);
                }
            } else {
                alert("Failed to view details ");
            }
        },
    });
}

//修改留言
function modifyContact(id) {
    $("#contactIdModify").val(id);
    $.ajax({
        url: "/contact/findById",
        type: "get",
        data: {id: id},
        success: function (result) {
            if (result.success) {
                $("#contentContactModify").val(result.contact.content);
            } else {
                alert("View details failed ");
            }
        },
    });
}

//检验联系方式名称是否重复
function checkSaveContactInformationName(type) {
    let name;
    let userId;
    if (type === 1) {
        name = $("#nameAddContactInformation").val();
        userId = $("#userIdAddContactInformation").val();
    } else if (type === 2) {
        name = $("#nameModifyContactInformation").val();
        userId = $("#userIdModifyContactInformation").val();
    }
    let nameIsExist;
    $.ajax({
        url: "/contactInformation/checkSaveContactInformationName",
        type: "get",
        async: false,
        data: {name: name, userId: userId},
        success: function (result) {
            if (result.success) {
                nameIsExist = true;
                alert("Sorry, "+ name + " already exist, please try others ");
            } else {
                nameIsExist = false;
            }
        },
    });
    return nameIsExist !== true;
}

//查看联系方式详情
function seeContactInformationDetails(id) {
    $.ajax({
        url: "/contactInformation/findById",
        type: "get",
        data: {id: id},
        success: function (result) {
            if (result.success) {
                $("#nameSeeContactInformation").val(result.contactInformation.name);
                $("#contentSeeContactInformation").val(result.contactInformation.content);
            } else {
                alert("View details failed ");
            }
        },
    });
}

//修改联系方式
function modifyContactInformation(id) {
    $("#idModifyContactInformation").val(id);
    $.ajax({
        url: "/contactInformation/findById",
        type: "get",
        data: {id: id},
        success: function (result) {
            if (result.success) {
                $("#nameModifyContactInformation").val(result.contactInformation.name);
                $("#contentModifyContactInformation").val(result.contactInformation.content);
            } else {
                alert("View details failed ");
            }
        },
    });
}

//删除联系方式
function deleteContactInformation(id) {
    if (confirm("Are you sure to delete this? ")) {
        window.location.href = "/contactInformation/delete?id=" + id;
    }
}

//验证商品详情是否为空
function checkAddGoodsValue() {
    let content = CKEDITOR.instances.contentGoods.getData();
    if (content === "" || content === null) {
        alert("Cannot be empty");
        return false;
    }
    return true;
}

//重置搜索商品的条件(用户商品管理),//TODO 重置功能未实现
function resetSearchGoodsValue() {
    $("#nameSearchGoods").val("");
    $("#goodsTypeIdSearchGoods").val("");
    $("#stateSearchGoods").val("");
    $("#isRecommendSearchGoods").val("");
}

// 添加商品到购物车 TODO 购物车
function addGoodsToShoppingCart(goodsId) {
    if (confirm("Are you sure put this to your shopping cart? ")) {//您确定要将这个商品加入购物车吗?
        $.ajax({
            url: "/goods/addGoodsToShoppingCart",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("Add to Cart successful ");//加入购物车成功！！
                } else {
                    alert(result.errorInfo);
                }
            },
        });
    }
}

//删除购物车的商品
function deleteGoodsInShoppingCart(goodsId) {
    if (confirm("Are you sure to delete? ")) {//您确定要将这个商品从购物车中删除吗?
        $.ajax({
            url: "/goods/deleteGoodsInShoppingCart",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("successfully deleted ");//todo 是不是这个有问题, 试试addGoodsToShoppingCart也不行
                    window.location.href = "/toMyShoppingCart";
                } else {
                    alert(result.errorInfo);
                }
            },
        });
    }
}

// 预订商品
function reserve(goodsId) {
    if (confirm("Are you sure to book? ")) {
        $.ajax({
            url: "/reserveRecord/reserve",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("Successfully booked, please contact the seller for transaction");
                    $.ajax({
                        url: "/goods/deleteGoodsInShoppingCart",
                        type: "post",
                        data: {goodsId: goodsId},
                        success: function (result) {
                            if (result.success) {
                                window.location.href = "/toMyReserveRecordPage";
                            } else {
                                alert(result.errorInfo);
                            }
                        },
                    });
                } else {
                    alert("Booking failed ");
                }
            },
        });
    }
}

//修改商品状态
function updateGoodsState(goodsId, state) {
    let stateName;
    if (state === 1) {
        stateName = "On the shelves";//上架
    } else if (state === 3) {
        stateName = "Remove from shelves";//下架
    } else if (state === 5) {
        stateName = "Transaction Done";//完成交易
    }
    if (confirm("Are you sure to set the state as " + stateName + " ?")) {
        $.ajax({
            url: "/goods/updateGoodsState",
            type: "post",
            data: {goodsId: goodsId, state: state},
            success: function (result) {
                if (result.success) {
                    alert("Set successfully");
                    window.location.href = "/toGoodsManagePage";
                } else {
                    alert("Set fail");//设置失败
                }
            },
        });
    }
}

//删除商品
function deleteGoods(goodsId) {
    if (confirm("Are you sure to delete the product?")) {
        $.ajax({
            url: "/goods/delete",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("Delete successfully");//删除成功！
                    window.location.href = "/toGoodsManagePage";
                } else {
                    alert("Delete failed");//删除失败！！
                }
            },
        });
    }
}

//查看商品详情
function seeOrModifyGoodsDetails(goodsId, type) {

    if (type === 1) {
        $("#modalHeadName").html("View");//查看
        $("#modifyButton").css("display", "none");
    } else if (type === 2) {
        $("#modalHeadName").html("Modify");//修改
        $("#modifyButton").css("display", "block");
    }
    $.ajax({
        url: "/goods/findById",
        type: "post",
        data: {goodsId: goodsId},
        success: function (result) {
            if (result.success) {
                $("#id").val(result.goods.id);
                $("#goodsName").val(result.goods.name);
                $("#priceNow").val(result.goods.priceNow);
                $("#goodsTypeId").val(result.goods.goodsTypeId);
                CKEDITOR.instances.contentGoods.setData(result.goods.content);
            } else {
                alert("Delete failed");
            }
        },
    });
}

//获取卖家联系方式
function getContactInformation(goodsId) {

    $.ajax({
        url: "/contactInformation/getListByGoodsId",
        type: "post",
        data: {goodsId: goodsId},
        success: function (result) {
            if (result.success) {
                $("#contactInformationStr").html(result.contactInformationStr);
            } else {
                alert("Delete failed");
            }
        },
    });
}

//修改预订记录状态
function updateReserveRecordState(reserveRecordId, state, stateNow) {

    let stateName;
    if (state === 1) {
        stateName = "The order has be cancelled";//预订已取消
    }
    if (stateNow === 1) {
        alert("The order has be cancelled");//无需操作,状态已经是预订已取消！！
        return false;
    }
    if (confirm("Are you sure set order state as " + stateName + " ?")) {
        $.ajax({
            url: "/reserveRecord/updateReserveRecordState",
            type: "post",
            data: {reserveRecordId: reserveRecordId, state: state},
            success: function (result) {
                if (result.success) {
                    alert("Set successfully");
                    window.location.href = "/toMyReserveRecordPage";
                } else {
                    alert("Set failed");//设置失败
                }
            },
        });
    }
}

//登录前检验用户是否被封禁
function checkLoginUserState() {

    let userName = $("#userName").val();
    let isBan = false;
    $.ajax({
        url: "/user/checkLoginUserState",
        type: "post",
        data: {userName: userName},
        async: false,
        success: function (result) {
            if (result.success) {
                let status = result.status;
                if (status === 0) {
                    isBan = true;
                }
            }
        },
    });
    if (isBan === true) {//账号被封，联系管理员解封
        alert("Your account has be locked, please contact admin, admin email: iamcriss44@gmail.com");
        return false;
    }
    return true;
}

//下拉框联动
function getGoodsNameTestPage() {
    let goodsTypeId = $("#goodsTypeIdTestPage").val();
    //当goodsTypeId为空,就结束方法并添加默认选项
    if (goodsTypeId === "") {
        $("#goodsNameTestPage").empty().append('<option>Select product name...</option>');//选择商品名称
        return false;
    }
    $.ajax({
        type: "get",
        url: "/getGoodsListByGoodsTypeId?goodsTypeId=" + goodsTypeId,
        success: function (result) {
            let goods = $("#goodsNameTestPage").empty();
            //添加默认选项
            goods.append('<option>Select product name...</option>');//选择商品名称
            for (let i = 0; i < result.length; i++) {
                goods.append("<option value='" + result[i].id + "'>" + result[i].name + "</option>");
            }
        }
    });
}