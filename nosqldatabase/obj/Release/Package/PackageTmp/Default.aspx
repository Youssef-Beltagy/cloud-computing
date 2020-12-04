<%@ Page Title="Home Page" Language="C#" MasterPageFile="~/Site.Master" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="nosqldatabase._Default" %>

<asp:Content ID="BodyContent" ContentPlaceHolderID="MainContent" runat="server">

    <asp:Button ID="LoadButton" runat="server" Text="Load" OnClick="LoadButton_Click" />

    <asp:Button ID="ClearButton" runat="server" OnClick="ClearButton_Click" Text="Clear" />

    <asp:Button ID="QueryButton" runat="server" OnClick="QueryButton_Click" Text="Query" />

    <asp:TextBox ID="TextBox1" runat="server"></asp:TextBox>

</asp:Content>
