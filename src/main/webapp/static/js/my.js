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
        val.value = "获取验证码";
        countdown = 60;
        return false;
    } else {
        val.setAttribute("disabled", true);
        val.className = 'btn-default btn btn-sm disabled';
        val.value = "重新发送(" + countdown + ")";
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

//检验Username是否存在
function checkUserName(userName) {
    if (userName === "") {
        $("#userNameFail").html("Username cannot be empty");
        $("#userNameSuccess").html("");
        $("#userName").focus();
        return;
    }
    $.post("/user/existUserWithUserName", {userName: userName},
        function (result) {
            if (result.success) {
                $("#userNameFail").html("Username exist");
                $("#userNameSuccess").html("");
                $("#userName").focus();
            } else {
                $("#userNameSuccess").html("Username can be used");
                $("#userNameFail").html("");
            }
        }
    );
}

//检验邮箱是否存在--注册时
function checkEmail(email) {
    if (email === "") {
        $("#emailFail").html("Email cannot be empty");
        $("#emailSuccess").html("");
        $("#emailRegister").focus();
        return;
    }
    $.post("/user/existEmail", {email: email},
        function (result) {
            if (result.success) {
                $("#emailFail").html("该邮箱已被注册,请重新输入后再注册!");
                $("#emailSuccess").html("");
                $("#emailRegister").focus();
            } else {
                $("#emailFail").html("");
                $("#emailSuccess").html("邮箱可以使用!");
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
            alert("你输入的邮箱地址没有在本站注册!!")
            return false;
        }
    }
    if (email == null || email === "") {
        alert("邮箱Cannot be empty!!");
        return false;
    }
    $.ajax({
        url: "/user/getVerificationCode",
        type: "get",
        data: {type: type, email: email},
        success: function (result) {
            if (result.success) {
                if (type === 1) {
                    alert("Verify code already sent, Go to check your email ")
                } else if (type === 2) {
                    alert("Verify code already sent, Go to check your email")
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
        alert("The password length must be greater than 5");
        return false;
    }
    if (password !== password2) {
        alert("The password and confirm password are different, please re-enter!");
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
        alert("Wrong code, please re-enter!");
        return false;
    }
    return true;
}

function logoutUser() {
    if (confirm("Are you sure logout?")) {
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
        alert("The password length must be greater than 5!");
        return false;
    }
    if (password !== password2) {
        alert("The password and confirm password are different, please re-enter!");
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
        alert("Wrong code, please re-enter!");
        return false;
    }
    return true;
}

//修改个人信息时验证
function checkModifyValue() {
    let password = $("#passwordModify").val();
    let password2 = $("#password2Modify").val();
    if (password !== password2) {
        alert("The password and confirm password are different, please re-enter!");
        return false;
    }
    return true;
}

//留言时验证
function checkContactValue() {
    let userId = $("#userId").val();
    if (userId == null || userId === '') {
        alert("Your login status has expired, please log in again");
        return false;
    }
    return true;
}

//Delete留言
function deleteContact(id) {
    if (confirm("Are you sure you want to delete this message?")) {
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
                    $("#contactReply").html("<span style='color: red'>Not Reply</span>");
                } else {
                    $("#contactReply").html(result.contact.reply);
                }
            } else {
                alert("View details failed！！");
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
                alert("View details failed！！");
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
                alert( name + "Already Exist！！");
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
                alert("View details failed！！");
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
                alert("View details failed！！");
            }
        },
    });
}

//Delete联系方式
function deleteContactInformation(id) {
    if (confirm("Are you sure to delete?")) {
        window.location.href = "/contactInformation/delete?id=" + id;
    }
}

//验证商品详情是否为空
function checkAddGoodsValue() {
    let content = CKEDITOR.instances.contentGoods.getData();
    if (content === "" || content === null) {
        alert("Cannot be empty!");
        return false;
    }
    return true;
}

//重置搜索商品的条件(用户商品管理)
function resetSearchGoodsValue() {
    $("#nameSearchGoods").val("");
    $("#goodsTypeIdSearchGoods").val("");
    $("#stateSearchGoods").val("");
    $("#isRecommendSearchGoods").val("");
}

// 添加商品到购物车
function addGoodsToShoppingCart(goodsId) {
    if (confirm("Are you sure to add this product to your shopping cart?")) {
        $.ajax({
            url: "/goods/addGoodsToShoppingCart",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("Added to the shopping cart successfully！！");
                } else {
                    alert(result.errorInfo);
                }
            },
        });
    }
}

//Delete购物车的商品
function deleteGoodsInShoppingCart(goodsId) {
    if (confirm("Are you sure delete this from shopping cart ")) {
        $.ajax({
            url: "/goods/deleteGoodsInShoppingCart",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("successfully deleted！！");
                    window.location.href = "/toMyShoppingCart";
                } else {
                    alert(result.errorInfo);
                }
            },
        });
    }
}

// 购买商品
function reserve(goodsId) {
    $.ajax({
        url: "/reserveRecord/reserve",
        type: "post",
        data: {goodsId: goodsId},
        success: function (result) {
            if (result.success) {
                alert("Payment successful");
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
                alert("Failed purchase！！");
            }
        },
    });
}

//修改商品状态
function updateGoodsState(goodsId, state) {
    let stateName;
    if (state === 1) {
        stateName = "On sale";
    } else if (state === 3) {
        stateName = "Off sale";
    } else if (state === 5) {
        stateName = "Deal done";
    }
    if (confirm("Are you sure to set the status be " + stateName + " ?")) {
        $.ajax({
            url: "/goods/updateGoodsState",
            type: "post",
            data: {goodsId: goodsId, state: state},
            success: function (result) {
                if (result.success) {
                    alert("Setup successful！！");
                    window.location.href = "/toGoodsManagePage";
                } else {
                    alert("Setup failed！！");
                }
            },
        });
    }
}

//Delete商品
function deleteGoods(goodsId) {
    if (confirm("Are you sure to delete this product?")) {
        $.ajax({
            url: "/goods/delete",
            type: "post",
            data: {goodsId: goodsId},
            success: function (result) {
                if (result.success) {
                    alert("Delete successful！！");
                    window.location.href = "/toGoodsManagePage";
                } else {
                    alert("Delete failed！！");
                }
            },
        });
    }
}

//查看商品详情
function seeOrModifyGoodsDetails(goodsId, type) {

    if (type === 1) {
        $("#modalHeadName").html("Check");
        $("#modifyButton").css("display", "none");
    } else if (type === 2) {
        $("#modalHeadName").html("Edit");
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
                alert("Delete Failed！！");
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
                alert("Delete Failed！！");
            }
        },
    });
}

//修改购买记录状态
function updateReserveRecordState(reserveRecordId, state, stateNow) {

    let stateName;
    if (state === 1) {
        stateName = "Cancel Deal";
    }
    if (stateNow === 1) {
        alert("The status is already transaction canceled！！");
        return false;
    }
    if (confirm("Are you sure to set the status be " + stateName + " ?")) {
        $.ajax({
            url: "/reserveRecord/updateReserveRecordState",
            type: "post",
            data: {reserveRecordId: reserveRecordId, state: state},
            success: function (result) {
                if (result.success) {
                    alert("Set successful！！");
                    window.location.href = "/toMyReserveRecordPage";
                } else {
                    alert("Set failed！！");
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
    if (isBan === true) {
        alert("Your account has been banned, Please contact admin: 123141@qq.com");
        return false;
    }
    return true;
}

//下拉框联动
function getGoodsNameTestPage() {
    let goodsTypeId = $("#goodsTypeIdTestPage").val();
    //当goodsTypeId为空,就结束方法并添加默认选项
    if (goodsTypeId === "") {
        $("#goodsNameTestPage").empty().append('<option> Select product name ...</option>');
        return false;
    }
    $.ajax({
        type: "get",
        url: "/getGoodsListByGoodsTypeId?goodsTypeId=" + goodsTypeId,
        success: function (result) {
            let goods = $("#goodsNameTestPage").empty();
            //添加默认选项
            goods.append('<option>Select product name ...</option>');
            for (let i = 0; i < result.length; i++) {
                goods.append("<option value='" + result[i].id + "'>" + result[i].name + "</option>");
            }
        }
    });
}

function seeShippingAddressDetails(id) {
    $.ajax({
        url: "/shippingAddress/findById",
        type: "get",
        data: {id: id},
        success: function (result) {
            if (result.success) {
                $("#contentSeeShippingAddress").val(result.shippingAddress.content);
            } else {
                alert("Check detail failed！！");
            }
        },
    });
}

function modifyShippingAddress(id) {
    $("#idModifyShippingAddress").val(id);
    $.ajax({
        url: "/shippingAddress/findById",
        type: "get",
        data: {id: id},
        success: function (result) {
            if (result.success) {
                $("#contentModifyShippingAddress").val(result.shippingAddress.content);
            } else {
                alert("Check detail failed！！");
            }
        },
    });
}

function deleteShippingAddress(id) {
    if (confirm("Are you sure to delete this address?")) {
        window.location.href = "/shippingAddress/delete?id=" + id;
    }
}

function setDefaultShippingAddress(id) {
    if (confirm("Are you sure to set it as the default delivery address?")) {
        window.location.href = "/shippingAddress/setDefaultShippingAddress?id=" + id;
    }
}

// 购买商品
function buyGoods(goodsId) {
    if (confirm("Are you sure to buy?")) {
        window.location.href = "/goods/buy?goodsId=" + goodsId;
    }
}

function payForGoods(goodsId) {
    if (confirm("Are you sure to pay?")) {
        window.location.href = "/goods/payForGoods?goodsId=" + goodsId;
    }
}