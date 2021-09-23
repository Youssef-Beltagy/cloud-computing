<%@ Page Title="Home Page" Language="C#" MasterPageFile="~/Site.Master" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="nosqldatabase_website._Default" %>

<asp:Content ID="BodyContent" ContentPlaceHolderID="MainContent" runat="server">

    <br />
    
    By Youssef Beltagy

    <br />
    <br />

    A database for the people and by the people.

    <br />
    <br />
    The data is loaded from Dr. Dimpsey's S3 object and loaded into the people DynamoDB table.
    
    <br />
    Load and clear are self-explanatory. Query must take a first name, a last name, or both.

    <br />
    <br />
    The SLA for this website as a whole is 99.74%. The overall SLA for the API the site uses is 99.79%.
    <br />
    To get finer SLA, I calculate SLAs per lambda. Load and Clear have 99.79% because they use S3 and DynamoDB.
    But query has 99.89% for its SLA. Query has a higher SLA because it doesn't use S3.
    <br />
    <br />
    <br />

    <asp:Button ID="LoadBtn" runat="server" OnClick="LoadBtn_Click" Text="Load" Height="40px" Width="80px" />
    &nbsp;&nbsp;&nbsp;

    <asp:Button ID="ClearBtn" runat="server" OnClick="ClearBtn_Click" Text="Clear" Height="40px" Width="80px" />
    &nbsp;&nbsp;&nbsp;

    <asp:Button ID="QueryBtn" runat="server" OnClick="QueryBtn_Click" Text="Query" Height="40px" Width="80px" />
    <br />
    <br />
    &nbsp;
    <h3>First Name</h3><asp:TextBox ID="firstnameTxtbox" runat="server" Height="20px" Width="170px"></asp:TextBox>

    &nbsp;&nbsp;

    <h3>Last Name</h3><asp:TextBox ID="lastnameTxtbox" runat="server" Height="20px" Width="170px"></asp:TextBox>

    <br />
    <h3>Output:</h3>
    <asp:Label ID="output" runat="server"></asp:Label>
</asp:Content>
